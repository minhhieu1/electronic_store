package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.auth.AuthResponse;
import com.altech.electronicstore.dto.auth.LoginRequest;
import com.altech.electronicstore.dto.auth.LogoutResponse;
import com.altech.electronicstore.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and blacklist JWT token")
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader);
            return ResponseEntity.ok(new LogoutResponse("Successfully logged out", true));
        }
        return ResponseEntity.badRequest().body(new LogoutResponse("No token provided", false));
    }
}
