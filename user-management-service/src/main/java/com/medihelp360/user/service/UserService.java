package com.medihelp360.user.service;

import com.medihelp360.user.domain.Role;
import com.medihelp360.user.domain.User;
import com.medihelp360.user.domain.UserStatus;
import com.medihelp360.user.dto.CreateUserRequest;
import com.medihelp360.user.dto.UpdateUserRequest;
import com.medihelp360.user.dto.UserResponse;
import com.medihelp360.user.event.UserCreatedEvent;
import com.medihelp360.user.event.UserUpdatedEvent;
import com.medihelp360.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String USER_TOPIC = "user-events";
    
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        
        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        
        // Obtener roles desde la base de datos si se proporcionaron
        Set<Role> managedRoles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<String> roleNames = request.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
            managedRoles = roleService.getRolesByNames(roleNames);
        }
        
        // Crear usuario
        User user = User.builder()
            .email(request.getEmail())
            .name(request.getName())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .status(UserStatus.ACTIVE)
            .roles(managedRoles)
            .build();
        
        User savedUser = userRepository.save(user);
        
        // Publicar evento
        UserCreatedEvent event = new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
            savedUser.getStatus().toString()
        );
        
        publishEvent(event);
        
        log.info("User created successfully with ID: {}", savedUser.getId());
        return mapToResponse(savedUser);
    }
    
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        String previousStatus = user.getStatus().toString();
        
        // Actualizar campos
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getRoles() != null) {
            // Obtener roles desde la base de datos
            Set<String> roleNames = request.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
            Set<Role> managedRoles = roleService.getRolesByNames(roleNames);
            user.setRoles(managedRoles);
        }
        
        User savedUser = userRepository.save(user);
        
        // Publicar evento
        UserUpdatedEvent event = new UserUpdatedEvent(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()),
            savedUser.getStatus().toString(),
            previousStatus
        );
        
        publishEvent(event);
        
        log.info("User updated successfully with ID: {}", savedUser.getId());
        return mapToResponse(savedUser);
    }
    
    public UserResponse updateUserStatus(UUID userId, UserStatus status) {
        log.info("Updating user status with ID: {} to status: {}", userId, status);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        String previousStatus = user.getStatus().toString();
        user.setStatus(status);
        
        User savedUser = userRepository.save(user);
        
        // Publicar evento
        UserUpdatedEvent event = new UserUpdatedEvent(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()),
            savedUser.getStatus().toString(),
            previousStatus
        );
        
        publishEvent(event);
        
        log.info("User status updated successfully with ID: {}", savedUser.getId());
        return mapToResponse(savedUser);
    }
    
    public void disableUser(UUID userId) {
        log.info("Disabling user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        String previousStatus = user.getStatus().toString();
        user.setStatus(UserStatus.DISABLED);
        
        User savedUser = userRepository.save(user);
        
        // Publicar evento
        UserUpdatedEvent event = new UserUpdatedEvent(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()),
            savedUser.getStatus().toString(),
            previousStatus
        );
        
        publishEvent(event);
        
        log.info("User disabled successfully with ID: {}", savedUser.getId());
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        return mapToResponse(user);
    }
    
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Getting all users with pagination: {}", pageable);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String name, String email, Pageable pageable) {
        log.info("Searching users with name: {} and email: {}", name, email);
        
        String searchName = name != null ? name : "";
        String searchEmail = email != null ? email : "";
        
        Page<User> users = userRepository.findByNameContainingOrEmailContaining(
            searchName, searchEmail, pageable);
        return users.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByStatus(UserStatus status) {
        log.info("Getting users by status: {}", status);
        List<User> users = userRepository.findByStatus(status);
        return users.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String roleName) {
        log.info("Getting users by role: {}", roleName);
        List<User> users = userRepository.findByRoleName(roleName.toUpperCase());
        return users.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        log.info("Getting user statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Contar usuarios por estado
        for (UserStatus status : UserStatus.values()) {
            Long count = userRepository.countByStatus(status);
            stats.put(status.toString().toLowerCase() + "Users", count);
        }
        
        // Total de usuarios
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        return stats;
    }
    
    private void publishEvent(Object event) {
        try {
            kafkaTemplate.send(USER_TOPIC, event);
            log.info("Event published: {}", event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getClass().getSimpleName(), e);
            // En un escenario real, podrías usar un patrón Outbox para garantizar eventual consistency
        }
    }
    
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .status(user.getStatus())
            .roles(user.getRoles())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
} 