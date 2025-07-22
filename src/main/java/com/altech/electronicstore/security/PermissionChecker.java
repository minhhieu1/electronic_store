package com.altech.electronicstore.security;

import com.altech.electronicstore.entity.Permission;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("permissionChecker")
@RequiredArgsConstructor
public class PermissionChecker {
    
    private final UserRepository userRepository;

    public boolean hasPermission(String permissionName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(user -> user.hasPermission(permissionName))
                .orElse(false);
    }

    public boolean hasPermission(String resource, String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(user -> user.hasPermission(resource, action))
                .orElse(false);
    }

    public boolean hasAnyPermission(String... permissionNames) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }

        Set<Permission> userPermissions = user.getAllPermissions();
        for (String permissionName : permissionNames) {
            if (userPermissions.stream().anyMatch(p -> p.getName().equals(permissionName))) {
                return true;
            }
        }
        return false;
    }

    public boolean canAccessResource(String resource) {
        return hasPermission(resource, "READ") || hasPermission(resource, "MANAGE");
    }

    public boolean canModifyResource(String resource) {
        return hasPermission(resource, "CREATE") || 
               hasPermission(resource, "UPDATE") || 
               hasPermission(resource, "DELETE") || 
               hasPermission(resource, "MANAGE");
    }
}
