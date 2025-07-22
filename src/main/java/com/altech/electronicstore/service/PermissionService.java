package com.altech.electronicstore.service;

import com.altech.electronicstore.entity.Permission;
import com.altech.electronicstore.entity.Role;
import com.altech.electronicstore.repository.PermissionRepository;
import com.altech.electronicstore.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    public Set<Permission> getPermissionsByRoleName(String roleName) {
        return permissionRepository.findPermissionsByRoleName(roleName);
    }

    public Permission createPermission(String name, String description, String resource, String action) {
        Permission permission = new Permission(name, description, resource, action);
        return permissionRepository.save(permission);
    }

    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }

    public Role assignPermissionToRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));
        
        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }

    public Role removePermissionFromRole(String roleName, String permissionName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));
        
        role.getPermissions().remove(permission);
        return roleRepository.save(role);
    }

}
