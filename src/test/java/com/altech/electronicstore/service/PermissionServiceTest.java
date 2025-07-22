package com.altech.electronicstore.service;

import com.altech.electronicstore.entity.Permission;
import com.altech.electronicstore.entity.Role;
import com.altech.electronicstore.repository.PermissionRepository;
import com.altech.electronicstore.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission testPermission;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testPermission = new Permission();
        testPermission.setId(1L);
        testPermission.setName("READ_PRODUCTS");
        testPermission.setDescription("Permission to read products");
        testPermission.setResource("products");
        testPermission.setAction("read");

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("CUSTOMER");
        testRole.setPermissions(new HashSet<>());
    }

    @Test
    void getAllPermissions_ShouldReturnAllPermissions() {
        // Given
        List<Permission> permissions = Arrays.asList(testPermission);
        when(permissionRepository.findAll()).thenReturn(permissions);

        // When
        List<Permission> result = permissionService.getAllPermissions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPermission.getId(), result.get(0).getId());
        verify(permissionRepository).findAll();
    }

    @Test
    void getAllPermissions_WhenNoPermissions_ShouldReturnEmptyList() {
        // Given
        when(permissionRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Permission> result = permissionService.getAllPermissions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(permissionRepository).findAll();
    }

    @Test
    void getPermissionById_WhenPermissionExists_ShouldReturnPermission() {
        // Given
        Long permissionId = 1L;
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));

        // When
        Optional<Permission> result = permissionService.getPermissionById(permissionId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPermission.getId(), result.get().getId());
        assertEquals(testPermission.getName(), result.get().getName());
        verify(permissionRepository).findById(permissionId);
    }

    @Test
    void getPermissionById_WhenPermissionNotExists_ShouldReturnEmpty() {
        // Given
        Long permissionId = 999L;
        when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

        // When
        Optional<Permission> result = permissionService.getPermissionById(permissionId);

        // Then
        assertFalse(result.isPresent());
        verify(permissionRepository).findById(permissionId);
    }

    @Test
    void getPermissionsByResource_WhenPermissionsExist_ShouldReturnPermissions() {
        // Given
        String resource = "products";
        List<Permission> permissions = Arrays.asList(testPermission);
        when(permissionRepository.findByResource(resource)).thenReturn(permissions);

        // When
        List<Permission> result = permissionService.getPermissionsByResource(resource);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPermission.getResource(), result.get(0).getResource());
        verify(permissionRepository).findByResource(resource);
    }

    @Test
    void getPermissionsByResource_WhenNoPermissions_ShouldReturnEmptyList() {
        // Given
        String resource = "nonexistent";
        when(permissionRepository.findByResource(resource)).thenReturn(Collections.emptyList());

        // When
        List<Permission> result = permissionService.getPermissionsByResource(resource);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(permissionRepository).findByResource(resource);
    }

    @Test
    void getPermissionsByRoleName_WhenPermissionsExist_ShouldReturnPermissions() {
        // Given
        String roleName = "CUSTOMER";
        Set<Permission> permissions = Set.of(testPermission);
        when(permissionRepository.findPermissionsByRoleName(roleName)).thenReturn(permissions);

        // When
        Set<Permission> result = permissionService.getPermissionsByRoleName(roleName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testPermission));
        verify(permissionRepository).findPermissionsByRoleName(roleName);
    }

    @Test
    void getPermissionsByRoleName_WhenNoPermissions_ShouldReturnEmptySet() {
        // Given
        String roleName = "NONEXISTENT";
        when(permissionRepository.findPermissionsByRoleName(roleName)).thenReturn(Collections.emptySet());

        // When
        Set<Permission> result = permissionService.getPermissionsByRoleName(roleName);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(permissionRepository).findPermissionsByRoleName(roleName);
    }

    @Test
    void createPermission_WhenValidData_ShouldCreatePermission() {
        // Given
        String name = "WRITE_PRODUCTS";
        String description = "Permission to write products";
        String resource = "products";
        String action = "write";

        Permission newPermission = new Permission(name, description, resource, action);
        newPermission.setId(2L);

        when(permissionRepository.save(any(Permission.class))).thenReturn(newPermission);

        // When
        Permission result = permissionService.createPermission(name, description, resource, action);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertEquals(resource, result.getResource());
        assertEquals(action, result.getAction());
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    void createPermission_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        String name = "INVALID_PERMISSION";
        String description = "Invalid permission";
        String resource = "invalid";
        String action = "invalid";

        when(permissionRepository.save(any(Permission.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                permissionService.createPermission(name, description, resource, action));
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    void deletePermission_WhenValidId_ShouldDeletePermission() {
        // Given
        Long permissionId = 1L;
        doNothing().when(permissionRepository).deleteById(permissionId);

        // When
        permissionService.deletePermission(permissionId);

        // Then
        verify(permissionRepository).deleteById(permissionId);
    }

    @Test
    void deletePermission_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        Long permissionId = 999L;
        doThrow(new RuntimeException("Permission not found"))
                .when(permissionRepository).deleteById(permissionId);

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                permissionService.deletePermission(permissionId));
        verify(permissionRepository).deleteById(permissionId);
    }

    @Test
    void assignPermissionToRole_WhenValidRoleAndPermission_ShouldAssignPermission() {
        // Given
        String roleName = "CUSTOMER";
        String permissionName = "READ_PRODUCTS";

        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(permissionName)).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        Role result = permissionService.assignPermissionToRole(roleName, permissionName);

        // Then
        assertNotNull(result);
        assertTrue(testRole.getPermissions().contains(testPermission));
        verify(roleRepository).findByName(roleName);
        verify(permissionRepository).findByName(permissionName);
        verify(roleRepository).save(testRole);
    }

    @Test
    void assignPermissionToRole_WhenRoleNotFound_ShouldThrowException() {
        // Given
        String roleName = "NONEXISTENT";
        String permissionName = "READ_PRODUCTS";

        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                permissionService.assignPermissionToRole(roleName, permissionName));
        assertEquals("Role not found: " + roleName, exception.getMessage());
        verify(roleRepository).findByName(roleName);
        verify(permissionRepository, never()).findByName(permissionName);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void assignPermissionToRole_WhenPermissionNotFound_ShouldThrowException() {
        // Given
        String roleName = "CUSTOMER";
        String permissionName = "NONEXISTENT";

        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(permissionName)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                permissionService.assignPermissionToRole(roleName, permissionName));
        assertEquals("Permission not found: " + permissionName, exception.getMessage());
        verify(roleRepository).findByName(roleName);
        verify(permissionRepository).findByName(permissionName);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void removePermissionFromRole_WhenValidRoleAndPermission_ShouldRemovePermission() {
        // Given
        String roleName = "CUSTOMER";
        String permissionName = "READ_PRODUCTS";
        
        testRole.getPermissions().add(testPermission); // Add permission first

        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(permissionName)).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        Role result = permissionService.removePermissionFromRole(roleName, permissionName);

        // Then
        assertNotNull(result);
        assertFalse(testRole.getPermissions().contains(testPermission));
        verify(roleRepository).findByName(roleName);
        verify(permissionRepository).findByName(permissionName);
        verify(roleRepository).save(testRole);
    }

    @Test
    void removePermissionFromRole_WhenRoleNotFound_ShouldThrowException() {
        // Given
        String roleName = "NONEXISTENT";
        String permissionName = "READ_PRODUCTS";

        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                permissionService.removePermissionFromRole(roleName, permissionName));
        assertEquals("Role not found: " + roleName, exception.getMessage());
        verify(roleRepository).findByName(roleName);
        verify(permissionRepository, never()).findByName(permissionName);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void removePermissionFromRole_WhenPermissionNotFound_ShouldThrowException() {
        // Given
        String roleName = "CUSTOMER";
        String permissionName = "NONEXISTENT";

        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(permissionName)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                permissionService.removePermissionFromRole(roleName, permissionName));
        assertEquals("Permission not found: " + permissionName, exception.getMessage());
        verify(roleRepository).findByName(roleName);
        verify(permissionRepository).findByName(permissionName);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void removePermissionFromRole_WhenPermissionNotInRole_ShouldStillSaveRole() {
        // Given
        String roleName = "CUSTOMER";
        String permissionName = "READ_PRODUCTS";
        
        // Don't add permission to role, so it's not there to remove

        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(permissionName)).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        Role result = permissionService.removePermissionFromRole(roleName, permissionName);

        // Then
        assertNotNull(result);
        assertFalse(testRole.getPermissions().contains(testPermission));
        verify(roleRepository).findByName(roleName);
        verify(permissionRepository).findByName(permissionName);
        verify(roleRepository).save(testRole);
    }
}
