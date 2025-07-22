package com.altech.electronicstore.controller;

import com.altech.electronicstore.dto.auth.AuthResponse;
import com.altech.electronicstore.dto.auth.LoginRequest;
import com.altech.electronicstore.dto.auth.LogoutResponse;
import com.altech.electronicstore.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthController authController;

    private LoginRequest createLoginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Given
        LoginRequest loginRequest = createLoginRequest("testuser", "password123");
        AuthResponse authResponse = new AuthResponse("jwt-token", "testuser", "USER");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("USER", response.getBody().getRole());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Given
        LoginRequest loginRequest = createLoginRequest("testuser", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        try {
            authController.login(loginRequest);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Invalid credentials", e.getMessage());
        }

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void logout_WithValidBearerToken_ShouldReturnSuccessResponse() {
        // Given
        String authHeader = "Bearer valid-jwt-token";
        when(httpServletRequest.getHeader("Authorization")).thenReturn(authHeader);
        doNothing().when(authService).logout(authHeader);

        // When
        ResponseEntity<LogoutResponse> response = authController.logout(httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Successfully logged out", response.getBody().getMessage());
        assertEquals(true, response.getBody().isSuccess());

        verify(httpServletRequest).getHeader("Authorization");
        verify(authService).logout(authHeader);
    }

    @Test
    void logout_WithInvalidTokenFormat_ShouldReturnBadRequest() {
        // Given
        String authHeader = "InvalidToken";
        when(httpServletRequest.getHeader("Authorization")).thenReturn(authHeader);

        // When
        ResponseEntity<LogoutResponse> response = authController.logout(httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No token provided", response.getBody().getMessage());
        assertEquals(false, response.getBody().isSuccess());

        verify(httpServletRequest).getHeader("Authorization");
        verify(authService, never()).logout(anyString());
    }

    @Test
    void logout_WithoutAuthorizationHeader_ShouldReturnBadRequest() {
        // Given
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        // When
        ResponseEntity<LogoutResponse> response = authController.logout(httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No token provided", response.getBody().getMessage());
        assertEquals(false, response.getBody().isSuccess());

        verify(httpServletRequest).getHeader("Authorization");
        verify(authService, never()).logout(anyString());
    }

    @Test
    void logout_WithEmptyAuthorizationHeader_ShouldReturnBadRequest() {
        // Given
        when(httpServletRequest.getHeader("Authorization")).thenReturn("");

        // When
        ResponseEntity<LogoutResponse> response = authController.logout(httpServletRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No token provided", response.getBody().getMessage());
        assertEquals(false, response.getBody().isSuccess());

        verify(httpServletRequest).getHeader("Authorization");
        verify(authService, never()).logout(anyString());
    }

    @Test
    void logout_WithBearerWithoutToken_ShouldCallService() {
        // Given
        String authHeader = "Bearer ";
        when(httpServletRequest.getHeader("Authorization")).thenReturn(authHeader);
        doNothing().when(authService).logout(authHeader);

        // When
        ResponseEntity<LogoutResponse> response = authController.logout(httpServletRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Successfully logged out", response.getBody().getMessage());
        assertEquals(true, response.getBody().isSuccess());

        verify(httpServletRequest).getHeader("Authorization");
        verify(authService).logout(authHeader);
    }

    @Test
    void logout_WhenServiceThrowsException_ShouldThrowException() {
        // Given
        String authHeader = "Bearer invalid-token";
        when(httpServletRequest.getHeader("Authorization")).thenReturn(authHeader);
        doThrow(new RuntimeException("Token validation failed"))
                .when(authService).logout(authHeader);

        // When & Then
        try {
            authController.logout(httpServletRequest);
            assertEquals(true, false, "Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Token validation failed", e.getMessage());
        }

        verify(httpServletRequest).getHeader("Authorization");
        verify(authService).logout(authHeader);
    }
}
