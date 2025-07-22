package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.permission.AssignPermissionRequest;
import com.altech.electronicstore.dto.permission.CreatePermissionRequest;
import com.altech.electronicstore.dto.permission.PermissionResponseDto;
import com.altech.electronicstore.dto.permission.RoleResponseDto;
import com.altech.electronicstore.entity.Permission;
import com.altech.electronicstore.entity.Role;
import com.altech.electronicstore.mapper.PermissionMapper;
import com.altech.electronicstore.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    @Mock
    private PermissionService permissionService;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionController permissionController;

    @Test
    void getAllPermissions_ShouldReturnListOfPermissions() {
        // Given
        Permission permission1 = createPermission(1L, "READ_PRODUCT", "PRODUCT", "READ");
        Permission permission2 = createPermission(2L, "WRITE_PRODUCT", "PRODUCT", "WRITE");
        List<Permission> permissions = Arrays.asList(permission1, permission2);

        PermissionResponseDto permissionDto1 = createPermissionResponseDto(1L, "READ_PRODUCT", "PRODUCT", "READ");
        PermissionResponseDto permissionDto2 = createPermissionResponseDto(2L, "WRITE_PRODUCT", "PRODUCT", "WRITE");
        List<PermissionResponseDto> permissionDtos = Arrays.asList(permissionDto1, permissionDto2);

        when(permissionService.getAllPermissions()).thenReturn(permissions);
        when(permissionMapper.toPermissionResponseDtoList(permissions)).thenReturn(permissionDtos);

        // When
        ResponseEntity<List<PermissionResponseDto>> response = permissionController.getAllPermissions();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("READ_PRODUCT", response.getBody().get(0).getName());
        assertEquals("WRITE_PRODUCT", response.getBody().get(1).getName());

        verify(permissionService).getAllPermissions();
        verify(permissionMapper).toPermissionResponseDtoList(permissions);
    }

    @Test
    void getPermissionById_WithValidId_ShouldReturnPermission() {
        // Given
        Long permissionId = 1L;
        Permission permission = createPermission(permissionId, "READ_PRODUCT", "PRODUCT", "READ");
        PermissionResponseDto permissionDto = createPermissionResponseDto(permissionId, "READ_PRODUCT", "PRODUCT", "READ");

        when(permissionService.getPermissionById(permissionId)).thenReturn(Optional.of(permission));
        when(permissionMapper.toPermissionResponseDto(permission)).thenReturn(permissionDto);

        // When
        ResponseEntity<PermissionResponseDto> response = permissionController.getPermissionById(permissionId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("READ_PRODUCT", response.getBody().getName());
        assertEquals("PRODUCT", response.getBody().getResource());
        assertEquals("READ", response.getBody().getAction());

        verify(permissionService).getPermissionById(permissionId);
        verify(permissionMapper).toPermissionResponseDto(permission);
    }

    @Test
    void getPermissionById_WithInvalidId_ShouldReturnNotFound() {
        // Given
        Long permissionId = 999L;

        when(permissionService.getPermissionById(permissionId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<PermissionResponseDto> response = permissionController.getPermissionById(permissionId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(permissionService).getPermissionById(permissionId);
        verify(permissionMapper, never()).toPermissionResponseDto(any());
    }

    @Test
    void getPermissionsByResource_ShouldReturnPermissionsForResource() {
        // Given
        String resource = "PRODUCT";
        Permission permission1 = createPermission(1L, "READ_PRODUCT", resource, "READ");
        Permission permission2 = createPermission(2L, "WRITE_PRODUCT", resource, "WRITE");
        List<Permission> permissions = Arrays.asList(permission1, permission2);

        PermissionResponseDto permissionDto1 = createPermissionResponseDto(1L, "READ_PRODUCT", resource, "READ");
        PermissionResponseDto permissionDto2 = createPermissionResponseDto(2L, "WRITE_PRODUCT", resource, "WRITE");
        List<PermissionResponseDto> permissionDtos = Arrays.asList(permissionDto1, permissionDto2);

        when(permissionService.getPermissionsByResource(resource)).thenReturn(permissions);
        when(permissionMapper.toPermissionResponseDtoList(permissions)).thenReturn(permissionDtos);

        // When
        ResponseEntity<List<PermissionResponseDto>> response = permissionController.getPermissionsByResource(resource);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("READ_PRODUCT", response.getBody().get(0).getName());
        assertEquals("WRITE_PRODUCT", response.getBody().get(1).getName());

        verify(permissionService).getPermissionsByResource(resource);
        verify(permissionMapper).toPermissionResponseDtoList(permissions);
    }

    @Test
    void getPermissionsByRole_ShouldReturnPermissionsForRole() {
        // Given
        String roleName = "ADMIN";
        Permission permission1 = createPermission(1L, "READ_PRODUCT", "PRODUCT", "READ");
        Permission permission2 = createPermission(2L, "WRITE_PRODUCT", "PRODUCT", "WRITE");
        Set<Permission> permissions = new HashSet<>(Arrays.asList(permission1, permission2));

        PermissionResponseDto permissionDto1 = createPermissionResponseDto(1L, "READ_PRODUCT", "PRODUCT", "READ");
        PermissionResponseDto permissionDto2 = createPermissionResponseDto(2L, "WRITE_PRODUCT", "PRODUCT", "WRITE");
        Set<PermissionResponseDto> permissionDtos = new HashSet<>(Arrays.asList(permissionDto1, permissionDto2));

        when(permissionService.getPermissionsByRoleName(roleName)).thenReturn(permissions);
        when(permissionMapper.toPermissionResponseDtoSet(permissions)).thenReturn(permissionDtos);

        // When
        ResponseEntity<Set<PermissionResponseDto>> response = permissionController.getPermissionsByRole(roleName);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(permissionService).getPermissionsByRoleName(roleName);
        verify(permissionMapper).toPermissionResponseDtoSet(permissions);
    }

    @Test
    void createPermission_WithValidRequest_ShouldReturnCreatedPermission() {
        // Given
        CreatePermissionRequest request = createPermissionRequest("DELETE_PRODUCT", "Delete product permission", "PRODUCT", "DELETE");
        Permission createdPermission = createPermission(1L, "DELETE_PRODUCT", "PRODUCT", "DELETE");
        PermissionResponseDto permissionDto = createPermissionResponseDto(1L, "DELETE_PRODUCT", "PRODUCT", "DELETE");

        when(permissionService.createPermission(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(createdPermission);
        when(permissionMapper.toPermissionResponseDto(createdPermission)).thenReturn(permissionDto);

        // When
        ResponseEntity<PermissionResponseDto> response = permissionController.createPermission(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DELETE_PRODUCT", response.getBody().getName());
        assertEquals("PRODUCT", response.getBody().getResource());
        assertEquals("DELETE", response.getBody().getAction());

        verify(permissionService).createPermission("DELETE_PRODUCT", "Delete product permission", "PRODUCT", "DELETE");
        verify(permissionMapper).toPermissionResponseDto(createdPermission);
    }

    @Test
    void assignPermissionToRole_WithValidRequest_ShouldReturnUpdatedRole() {
        // Given
        AssignPermissionRequest request = createAssignPermissionRequest("ADMIN", "READ_PRODUCT");
        Role updatedRole = createRole(1L, "ADMIN");
        RoleResponseDto roleDto = createRoleResponseDto(1L, "ADMIN");

        when(permissionService.assignPermissionToRole("ADMIN", "READ_PRODUCT")).thenReturn(updatedRole);
        when(permissionMapper.toRoleResponseDto(updatedRole)).thenReturn(roleDto);

        // When
        ResponseEntity<RoleResponseDto> response = permissionController.assignPermissionToRole(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ADMIN", response.getBody().getName());

        verify(permissionService).assignPermissionToRole("ADMIN", "READ_PRODUCT");
        verify(permissionMapper).toRoleResponseDto(updatedRole);
    }

    @Test
    void removePermissionFromRole_WithValidRequest_ShouldReturnUpdatedRole() {
        // Given
        AssignPermissionRequest request = createAssignPermissionRequest("USER", "WRITE_PRODUCT");
        Role updatedRole = createRole(2L, "USER");
        RoleResponseDto roleDto = createRoleResponseDto(2L, "USER");

        when(permissionService.removePermissionFromRole("USER", "WRITE_PRODUCT")).thenReturn(updatedRole);
        when(permissionMapper.toRoleResponseDto(updatedRole)).thenReturn(roleDto);

        // When
        ResponseEntity<RoleResponseDto> response = permissionController.removePermissionFromRole(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("USER", response.getBody().getName());

        verify(permissionService).removePermissionFromRole("USER", "WRITE_PRODUCT");
        verify(permissionMapper).toRoleResponseDto(updatedRole);
    }

    @Test
    void deletePermission_WithValidId_ShouldReturnOk() {
        // Given
        Long permissionId = 1L;
        doNothing().when(permissionService).deletePermission(permissionId);

        // When
        ResponseEntity<Void> response = permissionController.deletePermission(permissionId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(permissionService).deletePermission(permissionId);
    }

    @Test
    void deletePermission_WithInvalidId_ShouldThrowException() {
        // Given
        Long permissionId = 999L;
        doThrow(new RuntimeException("Permission not found with id: " + permissionId))
                .when(permissionService).deletePermission(permissionId);

        // When & Then
        try {
            permissionController.deletePermission(permissionId);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Permission not found with id: " + permissionId, e.getMessage());
        }

        verify(permissionService).deletePermission(permissionId);
    }

    // Helper methods
    private Permission createPermission(Long id, String name, String resource, String action) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setName(name);
        permission.setResource(resource);
        permission.setAction(action);
        return permission;
    }

    private PermissionResponseDto createPermissionResponseDto(Long id, String name, String resource, String action) {
        PermissionResponseDto dto = new PermissionResponseDto();
        dto.setId(id);
        dto.setName(name);
        dto.setResource(resource);
        dto.setAction(action);
        return dto;
    }

    private CreatePermissionRequest createPermissionRequest(String name, String description, String resource, String action) {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setName(name);
        request.setDescription(description);
        request.setResource(resource);
        request.setAction(action);
        return request;
    }

    private AssignPermissionRequest createAssignPermissionRequest(String roleName, String permissionName) {
        AssignPermissionRequest request = new AssignPermissionRequest();
        request.setRoleName(roleName);
        request.setPermissionName(permissionName);
        return request;
    }

    private Role createRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private RoleResponseDto createRoleResponseDto(Long id, String name) {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}
