package com.medihelp360.user.controller;

import com.medihelp360.user.domain.Role;
import com.medihelp360.user.dto.CreateRoleRequest;
import com.medihelp360.user.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {
    
    private final RoleService roleService;
    
    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Creating role with name: {}", request.getName());
        Role role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }
    
    @GetMapping("/{roleId}")
    public ResponseEntity<Role> getRoleById(@PathVariable UUID roleId) {
        log.info("Getting role by ID: {}", roleId);
        Role role = roleService.getRoleById(roleId);
        return ResponseEntity.ok(role);
    }
    
    @GetMapping("/name/{roleName}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String roleName) {
        log.info("Getting role by name: {}", roleName);
        Role role = roleService.getRoleByName(roleName);
        return ResponseEntity.ok(role);
    }
    
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("Getting all roles");
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleId) {
        log.info("Deleting role with ID: {}", roleId);
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }
} 