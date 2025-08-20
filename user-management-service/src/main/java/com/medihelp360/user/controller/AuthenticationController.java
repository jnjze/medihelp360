package com.medihelp360.user.controller;

import com.medihelp360.user.dto.LoginRequest;
import com.medihelp360.user.dto.LoginResponse;
import com.medihelp360.user.dto.RegisterRequest;
import com.medihelp360.user.dto.RegisterResponse;
import com.medihelp360.user.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, 
                                            HttpServletRequest httpRequest) {
        log.info("Login request received for user: {}", request.getEmail());
        
        // Extract IP address and user agent
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // Set IP address and user agent if not provided
        if (request.getIpAddress() == null) {
            request.setIpAddress(ipAddress);
        }
        if (request.getDeviceInfo() == null) {
            request.setDeviceInfo(userAgent);
        }
        
        try {
            LoginResponse response = authenticationService.login(request);
            log.info("Login successful for user: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Login failed for user: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request,
                                                   HttpServletRequest httpRequest) {
        log.info("Registration request received for email: {}", request.getEmail());
        
        // Extract IP address and user agent
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // Set IP address and user agent if not provided
        if (request.getIpAddress() == null) {
            request.setIpAddress(ipAddress);
        }
        if (request.getDeviceInfo() == null) {
            request.setDeviceInfo(userAgent);
        }
        
        try {
            RegisterResponse response = authenticationService.register(request);
            log.info("Registration successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Registration failed for email: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader,
                                     HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            authenticationService.logout(token, ipAddress, userAgent);
            log.info("Logout successful");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.warn("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            boolean isValid = authenticationService.validateToken(token);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestHeader("Authorization") String authorizationHeader,
                                                   HttpServletRequest httpRequest) {
        // TODO: Implement refresh token logic
        log.info("Refresh token request received");
        return ResponseEntity.status(501).build(); // Not implemented yet
    }
    
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header");
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
