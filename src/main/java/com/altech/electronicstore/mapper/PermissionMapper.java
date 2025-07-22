package com.altech.electronicstore.mapper;

import com.altech.electronicstore.dto.permission.PermissionResponseDto;
import com.altech.electronicstore.dto.permission.RoleResponseDto;
import com.altech.electronicstore.entity.Permission;
import com.altech.electronicstore.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionMapper {

    public PermissionResponseDto toPermissionResponseDto(Permission permission) {
        return PermissionResponseDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction())
                .build();
    }

    public RoleResponseDto toRoleResponseDto(Role role) {
        return RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(null) // Role entity doesn't have description field
                .permissions(role.getPermissions().stream()
                        .map(this::toPermissionResponseDto)
                        .collect(Collectors.toSet()))
                .build();
    }

    public List<PermissionResponseDto> toPermissionResponseDtoList(List<Permission> permissions) {
        return permissions.stream()
                .map(this::toPermissionResponseDto)
                .collect(Collectors.toList());
    }

    public Set<PermissionResponseDto> toPermissionResponseDtoSet(Set<Permission> permissions) {
        return permissions.stream()
                .map(this::toPermissionResponseDto)
                .collect(Collectors.toSet());
    }
}
