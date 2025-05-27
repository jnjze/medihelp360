package com.medihelp360.user.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserUpdatedEvent extends UserEvent {
    private UUID userId;
    private String email;
    private String name;
    private Set<String> roles;
    private String status;
    private String previousStatus;
    
    public UserUpdatedEvent(UUID userId, String email, String name, 
                           Set<String> roles, String status, String previousStatus) {
        super(userId);
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.status = status;
        this.previousStatus = previousStatus;
    }
} 