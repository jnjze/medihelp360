package com.medihelp360.user.controller;

import com.medihelp360.user.dto.LoginRequest;
import com.medihelp360.user.dto.LoginResponse;
import com.medihelp360.user.dto.RegisterRequest;
import com.medihelp360.user.dto.RegisterResponse;
import com.medihelp360.user.dto.UserResponse;
import com.medihelp360.user.dto.ErrorResponse;
import com.medihelp360.user.exception.AuthenticationException;
import com.medihelp360.user.exception.RegistrationException;
import com.medihelp360.user.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, 
                                 HttpServletRequest httpRequest) {
        log.info("Login request received for user: {}", request.getEmail());
        
        try {
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
            
            LoginResponse response = authenticationService.login(request);
            log.info("Login successful for user: {}", request.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {} - {} (Code: {})", 
                    request.getEmail(), e.getMessage(), e.getErrorCode());
            
            ErrorResponse errorResponse = ErrorResponse.authenticationError(
                e.getMessage(), 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid login request for user: {} - {}", request.getEmail(), e.getMessage());
            
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                e.getMessage(), 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {} - {}", 
                    request.getEmail(), e.getMessage(), e);
            
            ErrorResponse errorResponse = ErrorResponse.internalError(
                "An unexpected error occurred during login. Please try again later.", 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                    HttpServletRequest httpRequest) {
        log.info("Registration request received for email: {}", request.getEmail());
        
        try {
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
            
            RegisterResponse response = authenticationService.register(request);
            log.info("Registration successful for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RegistrationException e) {
            log.warn("Registration failed for email: {} - {} (Code: {})", 
                    request.getEmail(), e.getMessage(), e.getErrorCode());
            
            ErrorResponse errorResponse = ErrorResponse.registrationError(
                e.getMessage(), 
                httpRequest.getRequestURI(),
                e.getErrorCode()
            );
            return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid registration request for email: {} - {}", request.getEmail(), e.getMessage());
            
            String errorCode = "REG_001";
            if (e.getMessage().contains("Email already exists")) {
                errorCode = RegistrationException.EMAIL_ALREADY_EXISTS;
            } else if (e.getMessage().contains("Password and confirmation")) {
                errorCode = RegistrationException.PASSWORD_MISMATCH;
            }
            
            ErrorResponse errorResponse = ErrorResponse.registrationError(
                e.getMessage(), 
                httpRequest.getRequestURI(),
                errorCode
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error during registration for email: {} - {}", 
                    request.getEmail(), e.getMessage(), e);
            
            ErrorResponse errorResponse = ErrorResponse.internalError(
                "An unexpected error occurred during registration. Please try again later.", 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader,
                                  HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            authenticationService.logout(token, ipAddress, userAgent);
            log.info("Logout successful");
            return ResponseEntity.ok().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid logout request: {}", e.getMessage());
            
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                e.getMessage(), 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error during logout: {}", e.getMessage(), e);
            
            ErrorResponse errorResponse = ErrorResponse.internalError(
                "An unexpected error occurred during logout. Please try again later.", 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest httpRequest) {
        log.info("Get current user request received");
        
        try {
            // Extract JWT token from Authorization header
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                ErrorResponse errorResponse = ErrorResponse.unauthorized(
                    "Missing or invalid Authorization header", 
                    httpRequest.getRequestURI()
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            UserResponse userResponse = authenticationService.getCurrentUser(token);
            log.info("Current user retrieved successfully");
            return ResponseEntity.ok(userResponse);
            
        } catch (AuthenticationException e) {
            log.warn("Failed to get current user - {} (Code: {})", 
                    e.getMessage(), e.getErrorCode());
            
            ErrorResponse errorResponse = ErrorResponse.authenticationError(
                e.getMessage(), 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error while getting current user - {}", e.getMessage(), e);
            
            ErrorResponse errorResponse = ErrorResponse.internalError(
                "An unexpected error occurred while retrieving user information. Please try again later.", 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader,
                                         HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            boolean isValid = authenticationService.validateToken(token);
            return ResponseEntity.ok(isValid);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid token validation request: {}", e.getMessage());
            
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                e.getMessage(), 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage(), e);
            
            ErrorResponse errorResponse = ErrorResponse.internalError(
                "An unexpected error occurred during token validation. Please try again later.", 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authorizationHeader,
                                        HttpServletRequest httpRequest) {
        try {
            // TODO: Implement refresh token logic
            log.info("Refresh token request received");
            
            ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(501)
                .error("Not Implemented")
                .message("Refresh token functionality is not yet implemented")
                .path(httpRequest.getRequestURI())
                .errorCode("FEAT_001")
                .suggestion("Please login again to get a new access token")
                .build();
            
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error during refresh token: {}", e.getMessage(), e);
            
            ErrorResponse errorResponse = ErrorResponse.internalError(
                "An unexpected error occurred during refresh token. Please try again later.", 
                httpRequest.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
    
    // Global exception handlers for validation errors
    
    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::mapToValidationError)
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.validationError(
            "Validation failed for the request", 
            request.getRequestURI(),
            validationErrors
        );
        
        log.warn("Validation error for {}: {} errors", request.getRequestURI(), validationErrors.size());
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle constraint violation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                 HttpServletRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
            .stream()
            .map(this::mapToValidationError)
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.validationError(
            "Constraint validation failed", 
            request.getRequestURI(),
            validationErrors
        );
        
        log.warn("Constraint violation for {}: {} errors", request.getRequestURI(), validationErrors.size());
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle missing request body
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex,
                                                                 HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.badRequest(
            "Invalid request body format. Please check your JSON syntax.", 
            request.getRequestURI()
        );
        
        log.warn("Message not readable for {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle missing authorization header
     */
    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(org.springframework.web.bind.MissingRequestHeaderException ex,
                                                           HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.badRequest(
            "Missing required header: " + ex.getHeaderName(), 
            request.getRequestURI()
        );
        
        log.warn("Missing header for {}: {}", request.getRequestURI(), ex.getHeaderName());
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // Helper methods
    
    private ErrorResponse.ValidationError mapToValidationError(FieldError fieldError) {
        return ErrorResponse.ValidationError.builder()
            .field(fieldError.getField())
            .message(fieldError.getDefaultMessage())
            .rejectedValue(fieldError.getRejectedValue() != null ? fieldError.getRejectedValue().toString() : null)
            .build();
    }
    
    private ErrorResponse.ValidationError mapToValidationError(ConstraintViolation<?> violation) {
        return ErrorResponse.ValidationError.builder()
            .field(violation.getPropertyPath().toString())
            .message(violation.getMessage())
            .rejectedValue(violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : null)
            .build();
    }
}
