package com.medihelp360.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "failed_login_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedLoginAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
    
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 1;
    
    @Column(name = "first_attempt_at", nullable = false)
    private LocalDateTime firstAttemptAt;
    
    @Column(name = "last_attempt_at", nullable = false)
    private LocalDateTime lastAttemptAt;
    
    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
    
    @PrePersist
    protected void onCreate() {
        firstAttemptAt = LocalDateTime.now();
        lastAttemptAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastAttemptAt = LocalDateTime.now();
    }
    
    public void incrementAttempts() {
        this.attemptCount++;
        this.lastAttemptAt = LocalDateTime.now();
    }
    
    public boolean isBlocked() {
        return blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }
    
    public void block(int minutes) {
        this.blockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }
    
    public void reset() {
        this.attemptCount = 0;
        this.blockedUntil = null;
    }
}
