package com.altech.electronicstore.mapper;

import com.altech.electronicstore.dto.permission.PermissionResponseDto;
import com.altech.electronicstore.dto.permission.RoleResponseDto;
import com.altech.electronicstore.entity.Permission;
import com.altech.electronicstore.entity.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PermissionMapperTest {

    @InjectMocks
    private PermissionMapper permissionMapper;

    @Test
    void toPermissionResponseDto_ShouldMapPermissionToDto() {
        // Given
        Permission permission = createPermission(1L, "READ_PRODUCTS", "Read products permission", 
                                               "products", "read");

        // When
        PermissionResponseDto result = permissionMapper.toPermissionResponseDto(permission);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("READ_PRODUCTS", result.getName());
        assertEquals("Read products permission", result.getDescription());
        assertEquals("products", result.getResource());
        assertEquals("read", result.getAction());
    }

    @Test
    void toPermissionResponseDto_WithNullDescription_ShouldMapCorrectly() {
        // Given
        Permission permission = createPermission(2L, "WRITE_ORDERS", null, "orders", "write");

        // When
        PermissionResponseDto result = permissionMapper.toPermissionResponseDto(permission);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("WRITE_ORDERS", result.getName());
        assertNull(result.getDescription());
        assertEquals("orders", result.getResource());
        assertEquals("write", result.getAction());
    }

    @Test
    void toRoleResponseDto_ShouldMapRoleWithPermissionsToDto() {
        // Given
        Permission permission1 = createPermission(1L, "READ_PRODUCTS", "Read products", 
                                                "products", "read");
        Permission permission2 = createPermission(2L, "WRITE_PRODUCTS", "Write products", 
                                                "products", "write");
        Permission permission3 = createPermission(3L, "DELETE_PRODUCTS", "Delete products", 
                                                "products", "delete");

        Role role = createRole(1L, "PRODUCT_MANAGER");
        role.setPermissions(new HashSet<>(Arrays.asList(permission1, permission2, permission3)));

        // When
        RoleResponseDto result = permissionMapper.toRoleResponseDto(role);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PRODUCT_MANAGER", result.getName());
        assertNull(result.getDescription()); // Role entity doesn't have description field
        assertEquals(3, result.getPermissions().size());
        
        // Check that all permissions are mapped
        Set<Long> permissionIds = new HashSet<>();
        for (PermissionResponseDto permDto : result.getPermissions()) {
            permissionIds.add(permDto.getId());
        }
        assertTrue(permissionIds.contains(1L));
        assertTrue(permissionIds.contains(2L));
        assertTrue(permissionIds.contains(3L));
        
        // Verify one permission in detail
        PermissionResponseDto readPermission = result.getPermissions().stream()
                .filter(p -> p.getId().equals(1L))
                .findFirst().orElse(null);
        assertNotNull(readPermission);
        assertEquals("READ_PRODUCTS", readPermission.getName());
        assertEquals("Read products", readPermission.getDescription());
        assertEquals("products", readPermission.getResource());
        assertEquals("read", readPermission.getAction());
    }

    @Test
    void toRoleResponseDto_WithNoPermissions_ShouldMapRoleWithEmptyPermissions() {
        // Given
        Role role = createRole(2L, "GUEST");
        role.setPermissions(new HashSet<>());

        // When
        RoleResponseDto result = permissionMapper.toRoleResponseDto(role);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("GUEST", result.getName());
        assertNull(result.getDescription());
        assertEquals(0, result.getPermissions().size());
    }

    @Test
    void toPermissionResponseDtoList_ShouldMapListOfPermissionsToListOfDtos() {
        // Given
        Permission permission1 = createPermission(1L, "CREATE_USER", "Create user permission", 
                                                "users", "create");
        Permission permission2 = createPermission(2L, "UPDATE_USER", "Update user permission", 
                                                "users", "update");
        Permission permission3 = createPermission(3L, "DELETE_USER", "Delete user permission", 
                                                "users", "delete");

        List<Permission> permissions = Arrays.asList(permission1, permission2, permission3);

        // When
        List<PermissionResponseDto> result = permissionMapper.toPermissionResponseDtoList(permissions);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        PermissionResponseDto dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("CREATE_USER", dto1.getName());
        assertEquals("Create user permission", dto1.getDescription());
        assertEquals("users", dto1.getResource());
        assertEquals("create", dto1.getAction());

        PermissionResponseDto dto2 = result.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("UPDATE_USER", dto2.getName());
        assertEquals("users", dto2.getResource());
        assertEquals("update", dto2.getAction());

        PermissionResponseDto dto3 = result.get(2);
        assertEquals(3L, dto3.getId());
        assertEquals("DELETE_USER", dto3.getName());
        assertEquals("users", dto3.getResource());
        assertEquals("delete", dto3.getAction());
    }

    @Test
    void toPermissionResponseDtoList_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        List<Permission> emptyList = Collections.emptyList();

        // When
        List<PermissionResponseDto> result = permissionMapper.toPermissionResponseDtoList(emptyList);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void toPermissionResponseDtoSet_ShouldMapSetOfPermissionsToSetOfDtos() {
        // Given
        Permission permission1 = createPermission(1L, "READ_ORDERS", "Read orders", "orders", "read");
        Permission permission2 = createPermission(2L, "WRITE_ORDERS", "Write orders", "orders", "write");

        Set<Permission> permissions = new HashSet<>(Arrays.asList(permission1, permission2));

        // When
        Set<PermissionResponseDto> result = permissionMapper.toPermissionResponseDtoSet(permissions);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Set<Long> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (PermissionResponseDto dto : result) {
            ids.add(dto.getId());
            names.add(dto.getName());
        }
        
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));
        assertTrue(names.contains("READ_ORDERS"));
        assertTrue(names.contains("WRITE_ORDERS"));
    }

    @Test
    void toPermissionResponseDtoSet_WithEmptySet_ShouldReturnEmptySet() {
        // Given
        Set<Permission> emptySet = new HashSet<>();

        // When
        Set<PermissionResponseDto> result = permissionMapper.toPermissionResponseDtoSet(emptySet);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void toRoleResponseDto_WithAdminRole_ShouldMapAllAdminPermissions() {
        // Given
        Permission createPerm = createPermission(1L, "CREATE_ALL", "Create anything", "*", "create");
        Permission readPerm = createPermission(2L, "READ_ALL", "Read anything", "*", "read");
        Permission updatePerm = createPermission(3L, "UPDATE_ALL", "Update anything", "*", "update");
        Permission deletePerm = createPermission(4L, "DELETE_ALL", "Delete anything", "*", "delete");

        Role adminRole = createRole(1L, "ADMIN");
        adminRole.setPermissions(new HashSet<>(Arrays.asList(createPerm, readPerm, updatePerm, deletePerm)));

        // When
        RoleResponseDto result = permissionMapper.toRoleResponseDto(adminRole);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ADMIN", result.getName());
        assertEquals(4, result.getPermissions().size());
        
        // Verify all admin permissions are present
        Set<String> permissionNames = new HashSet<>();
        for (PermissionResponseDto perm : result.getPermissions()) {
            permissionNames.add(perm.getName());
            assertEquals("*", perm.getResource()); // All admin permissions should have wildcard resource
        }
        
        assertTrue(permissionNames.contains("CREATE_ALL"));
        assertTrue(permissionNames.contains("READ_ALL"));
        assertTrue(permissionNames.contains("UPDATE_ALL"));
        assertTrue(permissionNames.contains("DELETE_ALL"));
    }

    @Test
    void toPermissionResponseDto_WithSpecialCharacters_ShouldMapCorrectly() {
        // Given
        Permission permission = createPermission(1L, "SPECIAL_PERMISSION_123", 
                                               "Permission with special chars: !@#$%^&*()", 
                                               "special-resource_123", "special-action.test");

        // When
        PermissionResponseDto result = permissionMapper.toPermissionResponseDto(permission);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("SPECIAL_PERMISSION_123", result.getName());
        assertEquals("Permission with special chars: !@#$%^&*()", result.getDescription());
        assertEquals("special-resource_123", result.getResource());
        assertEquals("special-action.test", result.getAction());
    }

    // Helper methods
    private Permission createPermission(Long id, String name, String description, 
                                      String resource, String action) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setName(name);
        permission.setDescription(description);
        permission.setResource(resource);
        permission.setAction(action);
        return permission;
    }

    private Role createRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }
}
