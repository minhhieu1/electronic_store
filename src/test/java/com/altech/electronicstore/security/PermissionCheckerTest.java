package com.altech.electronicstore.security;

import com.altech.electronicstore.entity.Permission;
import com.altech.electronicstore.entity.Role;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionCheckerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PermissionChecker permissionChecker;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void hasPermission_WithValidUserAndPermission_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String permissionName = "USER_READ";
        User user = createUserWithPermissions(username, permissionName);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.hasPermission(permissionName);

        // Then
        assertTrue(result);
    }

    @Test
    void hasPermission_WithValidUserButNoPermission_ShouldReturnFalse() {
        // Given
        String username = "testuser";
        String requestedPermission = "USER_DELETE";
        User user = createUserWithPermissions(username, "USER_READ");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.hasPermission(requestedPermission);

        // Then
        assertFalse(result);
    }

    @Test
    void hasPermission_WithNullAuthentication_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        boolean result = permissionChecker.hasPermission("ANY_PERMISSION");

        // Then
        assertFalse(result);
    }

    @Test
    void hasPermission_WithUnauthenticatedUser_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        boolean result = permissionChecker.hasPermission("ANY_PERMISSION");

        // Then
        assertFalse(result);
    }

    @Test
    void hasPermission_WithNonExistentUser_ShouldReturnFalse() {
        // Given
        String username = "nonexistent";
        String permissionName = "USER_READ";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        boolean result = permissionChecker.hasPermission(permissionName);

        // Then
        assertFalse(result);
    }

    @Test
    void hasPermissionResourceAction_WithValidPermission_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        String action = "READ";
        User user = createUserWithResourcePermission(username, resource, action);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.hasPermission(resource, action);

        // Then
        assertTrue(result);
    }

    @Test
    void hasPermissionResourceAction_WithInvalidPermission_ShouldReturnFalse() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        String action = "DELETE";
        User user = createUserWithResourcePermission(username, resource, "READ");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.hasPermission(resource, action);

        // Then
        assertFalse(result);
    }

    @Test
    void hasAnyPermission_WithOneValidPermission_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        User user = createUserWithPermissions(username, "USER_READ", "PRODUCT_READ");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.hasAnyPermission("USER_DELETE", "PRODUCT_READ", "ORDER_CREATE");

        // Then
        assertTrue(result);
    }

    @Test
    void hasAnyPermission_WithNoValidPermissions_ShouldReturnFalse() {
        // Given
        String username = "testuser";
        User user = createUserWithPermissions(username, "USER_READ");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.hasAnyPermission("USER_DELETE", "PRODUCT_DELETE", "ORDER_CREATE");

        // Then
        assertFalse(result);
    }

    @Test
    void hasAnyPermission_WithNullUser_ShouldReturnFalse() {
        // Given
        String username = "testuser";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        boolean result = permissionChecker.hasAnyPermission("USER_READ", "PRODUCT_READ");

        // Then
        assertFalse(result);
    }

    @Test
    void canAccessResource_WithReadPermission_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        User user = createUserWithResourcePermission(username, resource, "READ");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.canAccessResource(resource);

        // Then
        assertTrue(result);
    }

    @Test
    void canAccessResource_WithManagePermission_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        User user = createUserWithResourcePermission(username, resource, "MANAGE");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.canAccessResource(resource);

        // Then
        assertTrue(result);
    }

    @Test
    void canAccessResource_WithoutReadOrManagePermission_ShouldReturnFalse() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        User user = createUserWithResourcePermission(username, resource, "CREATE");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.canAccessResource(resource);

        // Then
        assertFalse(result);
    }

    @Test
    void canModifyResource_WithCreatePermission_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        User user = createUserWithResourcePermission(username, resource, "CREATE");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.canModifyResource(resource);

        // Then
        assertTrue(result);
    }

    @Test
    void canModifyResource_WithUpdatePermission_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        User user = createUserWithResourcePermission(username, resource, "UPDATE");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.canModifyResource(resource);

        // Then
        assertTrue(result);
    }

    @Test
    void canModifyResource_WithDeletePermission_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        User user = createUserWithResourcePermission(username, resource, "DELETE");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.canModifyResource(resource);

        // Then
        assertTrue(result);
    }

    @Test
    void canModifyResource_WithManagePermission_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        User user = createUserWithResourcePermission(username, resource, "MANAGE");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.canModifyResource(resource);

        // Then
        assertTrue(result);
    }

    @Test
    void canModifyResource_WithOnlyReadPermission_ShouldReturnFalse() {
        // Given
        String username = "testuser";
        String resource = "PRODUCT";
        User user = createUserWithResourcePermission(username, resource, "READ");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = permissionChecker.canModifyResource(resource);

        // Then
        assertFalse(result);
    }

    // Helper methods
    private User createUserWithPermissions(String username, String... permissionNames) {
        User user = new User();
        user.setUsername(username);
        
        Set<Permission> permissions = new HashSet<>();
        for (String permissionName : permissionNames) {
            Permission permission = new Permission();
            permission.setName(permissionName);
            permissions.add(permission);
        }
        
        Role role = new Role();
        role.setPermissions(permissions);
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        return user;
    }
    
    private User createUserWithResourcePermission(String username, String resource, String action) {
        User user = new User();
        user.setUsername(username);
        
        Permission permission = new Permission();
        permission.setResource(resource);
        permission.setAction(action);
        
        Set<Permission> permissions = new HashSet<>();
        permissions.add(permission);
        
        Role role = new Role();
        role.setPermissions(permissions);
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        return user;
    }
}
