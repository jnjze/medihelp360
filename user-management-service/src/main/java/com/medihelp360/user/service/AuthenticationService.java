package com.medihelp360.user.service;

import com.medihelp360.user.domain.*;
import com.medihelp360.user.dto.LoginRequest;
import com.medihelp360.user.dto.LoginResponse;
import com.medihelp360.user.repository.AccessLogRepository;
import com.medihelp360.user.repository.FailedLoginAttemptRepository;
import com.medihelp360.user.repository.UserRepository;
import com.medihelp360.user.repository.UserSessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final AccessLogRepository accessLogRepository;
    private final FailedLoginAttemptRepository failedLoginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:1800}") // 30 minutes default
    private Long jwtExpiration;
    
    @Value("${app.jwt.refresh-expiration:604800}") // 7 days default
    private Long refreshExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());
        
        // Check if IP is blocked
        if (isIpBlocked(request.getIpAddress())) {
            log.warn("Login blocked: IP {} is blocked", request.getIpAddress());
            recordFailedLogin(request.getEmail(), request.getIpAddress(), "IP_BLOCKED");
            throw new RuntimeException("Access temporarily blocked due to multiple failed attempts");
        }
        
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            log.warn("Login failed: User not found for email: {}", request.getEmail());
            recordFailedLogin(request.getEmail(), request.getIpAddress(), "USER_NOT_FOUND");
            throw new RuntimeException("Invalid credentials");
        }
        
        User user = userOpt.get();
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Login failed: Account locked for user: {}", request.getEmail());
            recordFailedLogin(request.getEmail(), request.getIpAddress(), "ACCOUNT_LOCKED");
            throw new RuntimeException("Account is temporarily locked. Please try again later.");
        }
        
        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Invalid password for user: {}", request.getEmail());
            user.recordFailedLogin();
            userRepository.save(user);
            recordFailedLogin(request.getEmail(), request.getIpAddress(), "INVALID_PASSWORD");
            throw new RuntimeException("Invalid credentials");
        }
        
        // Check if user is active
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login failed: Inactive user: {}", request.getEmail());
            recordFailedLogin(request.getEmail(), request.getIpAddress(), "INACTIVE_USER");
            throw new RuntimeException("Account is not active");
        }
        
        // Generate tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        
        // Record successful login
        user.recordSuccessfulLogin();
        userRepository.save(user);
        
        // Create session
        UserSession session = UserSession.builder()
                .user(user)
                .tokenHash(hashToken(accessToken))
                .refreshTokenHash(hashToken(refreshToken))
                .deviceInfo(request.getDeviceInfo())
                .ipAddress(request.getIpAddress())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration))
                .build();
        
        userSessionRepository.save(session);
        
        // Log successful login
        AccessLog successLog = AccessLog.loginSuccess(user, request.getIpAddress(), request.getDeviceInfo());
        accessLogRepository.save(successLog);
        
        // Reset failed attempts for this IP
        resetFailedAttempts(request.getEmail(), request.getIpAddress());
        
        log.info("Login successful for user: {}", request.getEmail());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpiration)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .name(user.getName())
                        .status(user.getStatus().name())
                        .roles(user.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet()))
                        .lastLogin(user.getLastLogin())
                        .build())
                .build();
    }
    
    @Transactional
    public void logout(String token, String ipAddress, String userAgent) {
        String tokenHash = hashToken(token);
        Optional<UserSession> sessionOpt = userSessionRepository.findByTokenHash(tokenHash);
        
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            User user = session.getUser();
            
            // Log logout
            AccessLog logoutLog = AccessLog.logout(user, ipAddress, userAgent);
            accessLogRepository.save(logoutLog);
            
            // Remove session
            userSessionRepository.delete(session);
            
            log.info("User logged out: {}", user.getEmail());
        }
    }
    
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            
            // Check if session exists and is not expired
            String tokenHash = hashToken(token);
            Optional<UserSession> sessionOpt = userSessionRepository.findByTokenHash(tokenHash);
            
            if (sessionOpt.isEmpty()) {
                return false;
            }
            
            UserSession session = sessionOpt.get();
            return !session.isExpired();
            
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    private String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = Date.from(now.toInstant().plusSeconds(jwtExpiration));
        
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    private String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = Date.from(now.toInstant().plusSeconds(refreshExpiration));
        
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    private String hashToken(String token) {
        // In production, use a proper hash function
        return token.substring(0, Math.min(token.length(), 50));
    }
    
    private boolean isIpBlocked(String ipAddress) {
        Optional<FailedLoginAttempt> attemptOpt = failedLoginAttemptRepository
                .findByEmailAndIpAddress("", ipAddress);
        
        if (attemptOpt.isPresent()) {
            FailedLoginAttempt attempt = attemptOpt.get();
            return attempt.isBlocked();
        }
        
        return false;
    }
    
    private void recordFailedLogin(String email, String ipAddress, String reason) {
        Optional<FailedLoginAttempt> attemptOpt = failedLoginAttemptRepository
                .findByEmailAndIpAddress(email, ipAddress);
        
        if (attemptOpt.isPresent()) {
            FailedLoginAttempt attempt = attemptOpt.get();
            attempt.incrementAttempts();
            if (attempt.getAttemptCount() >= 5) {
                attempt.block(30); // Block for 30 minutes
            }
            failedLoginAttemptRepository.save(attempt);
        } else {
            FailedLoginAttempt newAttempt = FailedLoginAttempt.builder()
                    .email(email)
                    .ipAddress(ipAddress)
                    .build();
            failedLoginAttemptRepository.save(newAttempt);
        }
        
        // Log failed attempt
        AccessLog failedLog = AccessLog.loginFailed(email, ipAddress, "", reason);
        accessLogRepository.save(failedLog);
    }
    
    private void resetFailedAttempts(String email, String ipAddress) {
        failedLoginAttemptRepository.deleteByEmailAndIpAddress(email, ipAddress);
    }
}
