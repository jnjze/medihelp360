package com.medihelp360.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    
    private String id;
    private String email;
    private String name;
    private String status;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private String message;
    private boolean requiresEmailVerification;
    
    // Factory methods for different scenarios
    public static RegisterResponse success(String id, String email, String name, 
                                         String status, Set<String> roles, 
                                         LocalDateTime createdAt) {
        return RegisterResponse.builder()
            .id(id)
            .email(email)
            .name(name)
            .status(status)
            .roles(roles)
            .createdAt(createdAt)
            .message("User registered successfully")
            .requiresEmailVerification(false)
            .build();
    }
    
    public static RegisterResponse successWithVerification(String id, String email, String name) {
        return RegisterResponse.builder()
            .id(id)
            .email(email)
            .name(name)
            .status("PENDING_VERIFICATION")
            .message("User registered successfully. Please check your email to verify your account.")
            .requiresEmailVerification(true)
            .build();
    }
}
