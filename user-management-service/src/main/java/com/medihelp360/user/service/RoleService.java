package com.medihelp360.user.service;

import com.medihelp360.user.domain.Role;
import com.medihelp360.user.dto.CreateRoleRequest;
import com.medihelp360.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    public Role createRole(CreateRoleRequest request) {
        log.info("Creating role with name: {}", request.getName());
        
        if (roleRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Role with name " + request.getName() + " already exists");
        }
        
        Role role = Role.builder()
            .name(request.getName().toUpperCase())
            .description(request.getDescription())
            .build();
        
        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", savedRole.getId());
        
        return savedRole;
    }
    
    @Transactional(readOnly = true)
    public Role getRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
    }
    
    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Role not found with name: " + name));
    }
    
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Set<Role> getRolesByNames(Set<String> names) {
        Set<String> upperCaseNames = names.stream()
            .map(String::toUpperCase)
            .collect(java.util.stream.Collectors.toSet());
        return roleRepository.findByNameIn(upperCaseNames);
    }
    
    public void deleteRole(UUID roleId) {
        log.info("Deleting role with ID: {}", roleId);
        
        if (!roleRepository.existsById(roleId)) {
            throw new IllegalArgumentException("Role not found with ID: " + roleId);
        }
        
        roleRepository.deleteById(roleId);
        log.info("Role deleted successfully with ID: {}", roleId);
    }
} 