package com.medihelp360.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "access_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "success", nullable = false)
    private Boolean success = true;
    
    @Column(name = "details", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String details; // JSON string for flexibility
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
    
    // Helper methods for common actions
    public static AccessLog loginSuccess(User user, String ipAddress, String userAgent) {
        return AccessLog.builder()
                .user(user)
                .action("LOGIN_SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
    }
    
    public static AccessLog loginFailed(String email, String ipAddress, String userAgent, String reason) {
        return AccessLog.builder()
                .action("LOGIN_FAILED")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(false)
                .details("{\"email\":\"" + email + "\",\"reason\":\"" + reason + "\"}")
                .build();
    }
    
    public static AccessLog buildAuditObject(String action, User user, String ipAddress, String userAgent) {
        return AccessLog.builder()
                .user(user)
                .action(action)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .build();
    }


}
