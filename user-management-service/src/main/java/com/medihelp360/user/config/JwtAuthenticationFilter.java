package com.medihelp360.user.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        log.info("JWT Filter: Processing request to: {}", request.getRequestURI());
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                log.info("JWT Filter: JWT token found, validating...");
                Claims claims = validateAndParseToken(jwt);
                
                if (claims != null) {
                    String userId = claims.getSubject();
                    String email = claims.get("email", String.class);
                    
                    @SuppressWarnings("unchecked")
                    List<String> roles = claims.get("roles", List.class);
                    
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("JWT Filter: Authentication successful for user: {} (ID: {}) with roles: {}", email, userId, roles);
                } else {
                    log.warn("JWT Filter: JWT token validation failed");
                }
            } else {
                log.info("JWT Filter: No JWT token found in request");
            }
        } catch (Exception e) {
            log.error("JWT Filter: Error during authentication: {}", e.getMessage(), e);
        }
        
        log.info("JWT Filter: Continuing with filter chain");
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Claims validateAndParseToken(String token) {
        try {
            SecretKey signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
                
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return null;
        }
    }
}
