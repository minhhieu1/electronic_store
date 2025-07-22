package com.altech.electronicstore.dto.permission;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AssignPermissionRequest {
    @NotBlank(message = "Role name is required")
    private String roleName;
    
    @NotBlank(message = "Permission name is required")
    private String permissionName;
}
