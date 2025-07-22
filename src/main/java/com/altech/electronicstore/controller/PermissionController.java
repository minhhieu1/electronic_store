package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.permission.AssignPermissionRequest;
import com.altech.electronicstore.dto.permission.CreatePermissionRequest;
import com.altech.electronicstore.dto.permission.PermissionResponseDto;
import com.altech.electronicstore.dto.permission.RoleResponseDto;
import com.altech.electronicstore.entity.Permission;
import com.altech.electronicstore.entity.Role;
import com.altech.electronicstore.mapper.PermissionMapper;
import com.altech.electronicstore.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "APIs for managing permissions and role-based access control")
@SecurityRequirement(name = "Bearer Authentication")
public class PermissionController {
    
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @GetMapping
    @Operation(summary = "Get all permissions", description = "Retrieve all available permissions in the system")
    @PreAuthorize("@permissionChecker.hasPermission('PERMISSION', 'READ')")
    public ResponseEntity<List<PermissionResponseDto>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        List<PermissionResponseDto> permissionDtos = permissionMapper.toPermissionResponseDtoList(permissions);
        return ResponseEntity.ok(permissionDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID", description = "Retrieve a specific permission by its ID")
    @PreAuthorize("@permissionChecker.hasPermission('PERMISSION', 'READ')")
    public ResponseEntity<PermissionResponseDto> getPermissionById(
            @Parameter(description = "Permission ID") @PathVariable Long id) {
        return permissionService.getPermissionById(id)
                .map(permissionMapper::toPermissionResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-resource/{resource}")
    @Operation(summary = "Get permissions by resource", description = "Retrieve all permissions for a specific resource")
    @PreAuthorize("@permissionChecker.hasPermission('PERMISSION', 'READ')")
    public ResponseEntity<List<PermissionResponseDto>> getPermissionsByResource(
            @Parameter(description = "Resource name") @PathVariable String resource) {
        List<Permission> permissions = permissionService.getPermissionsByResource(resource);
        List<PermissionResponseDto> permissionDtos = permissionMapper.toPermissionResponseDtoList(permissions);
        return ResponseEntity.ok(permissionDtos);
    }

    @GetMapping("/by-role/{roleName}")
    @Operation(summary = "Get permissions by role", description = "Retrieve all permissions assigned to a specific role")
    @PreAuthorize("@permissionChecker.hasPermission('PERMISSION', 'READ')")
    public ResponseEntity<Set<PermissionResponseDto>> getPermissionsByRole(
            @Parameter(description = "Role name") @PathVariable String roleName) {
        Set<Permission> permissions = permissionService.getPermissionsByRoleName(roleName);
        Set<PermissionResponseDto> permissionDtos = permissionMapper.toPermissionResponseDtoSet(permissions);
        return ResponseEntity.ok(permissionDtos);
    }

    @PostMapping
    @Operation(summary = "Create permission", description = "Create a new permission")
    @PreAuthorize("@permissionChecker.hasPermission('PERMISSION', 'CREATE')")
    public ResponseEntity<PermissionResponseDto> createPermission(@RequestBody CreatePermissionRequest request) {
        Permission permission = permissionService.createPermission(
                request.getName(),
                request.getDescription(),
                request.getResource(),
                request.getAction()
        );
        PermissionResponseDto permissionDto = permissionMapper.toPermissionResponseDto(permission);
        return ResponseEntity.ok(permissionDto);
    }

    @PostMapping("/assign-to-role")
    @Operation(summary = "Assign permission to role", description = "Assign a permission to a specific role")
    @PreAuthorize("@permissionChecker.hasPermission('ROLE', 'UPDATE')")
    public ResponseEntity<RoleResponseDto> assignPermissionToRole(@RequestBody AssignPermissionRequest request) {
        Role role = permissionService.assignPermissionToRole(request.getRoleName(), request.getPermissionName());
        RoleResponseDto roleDto = permissionMapper.toRoleResponseDto(role);
        return ResponseEntity.ok(roleDto);
    }

    @DeleteMapping("/remove-from-role")
    @Operation(summary = "Remove permission from role", description = "Remove a permission from a specific role")
    @PreAuthorize("@permissionChecker.hasPermission('ROLE', 'UPDATE')")
    public ResponseEntity<RoleResponseDto> removePermissionFromRole(@RequestBody AssignPermissionRequest request) {
        Role role = permissionService.removePermissionFromRole(request.getRoleName(), request.getPermissionName());
        RoleResponseDto roleDto = permissionMapper.toRoleResponseDto(role);
        return ResponseEntity.ok(roleDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission", description = "Delete a permission by its ID")
    @PreAuthorize("@permissionChecker.hasPermission('PERMISSION', 'DELETE')")
    public ResponseEntity<Void> deletePermission(
            @Parameter(description = "Permission ID") @PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok().build();
    }
}
