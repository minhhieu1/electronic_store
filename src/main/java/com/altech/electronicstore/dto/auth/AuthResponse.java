package com.altech.electronicstore.dto.auth;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String username;
    private String role;
    
    public AuthResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }
}
