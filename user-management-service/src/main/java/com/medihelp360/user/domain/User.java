package com.medihelp360.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Authentication fields
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "failed_attempts")
    @Builder.Default
    private Integer failedAttempts = 0;
    
    @Column(name = "account_locked")
    @Builder.Default
    private Boolean accountLocked = false;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @PrePersist
    protected void onCreate() {
        if (passwordChangedAt == null) {
            passwordChangedAt = LocalDateTime.now();
        }
    }
    
    public boolean isAccountLocked() {
        return accountLocked && lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }
    
    public void lockAccount(int minutes) {
        this.accountLocked = true;
        this.lockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }
    
    public void unlockAccount() {
        this.accountLocked = false;
        this.lockedUntil = null;
        this.failedAttempts = 0;
    }
    
    public void recordFailedLogin() {
        this.failedAttempts++;
        if (this.failedAttempts >= 5) {
            lockAccount(30); // Lock for 30 minutes after 5 failed attempts
        }
    }
    
    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        this.failedAttempts = 0;
        this.accountLocked = false;
        this.lockedUntil = null;
    }
} 