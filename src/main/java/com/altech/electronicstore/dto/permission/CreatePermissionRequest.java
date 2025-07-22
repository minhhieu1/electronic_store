package com.altech.electronicstore.dto.permission;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreatePermissionRequest {
    @NotBlank(message = "Permission name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Resource is required")
    private String resource;
    
    @NotBlank(message = "Action is required")
    private String action;
}
