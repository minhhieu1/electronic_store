package com.altech.electronicstore.service;

import com.altech.electronicstore.dto.auth.AuthResponse;
import com.altech.electronicstore.dto.auth.LoginRequest;
import com.altech.electronicstore.entity.User;
import com.altech.electronicstore.repository.UserRepository;
import com.altech.electronicstore.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void login_WhenValidCredentials_ShouldReturnAuthResponse() {
        // Given
        String expectedToken = "jwt-token-123";
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getAuthorities()).thenReturn((Collection) authorities);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("testuser", result.getUsername());
        assertEquals("ROLE_CUSTOMER", result.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void login_WhenInvalidCredentials_ShouldThrowBadCredentialsException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void login_WhenUserHasNoAuthorities_ShouldReturnDefaultRole() {
        // Given
        String expectedToken = "jwt-token-123";
        Collection<GrantedAuthority> emptyAuthorities = Collections.emptyList();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getAuthorities()).thenReturn((Collection) emptyAuthorities);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("testuser", result.getUsername());
        assertEquals("CUSTOMER", result.getRole()); // Default role

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void login_WhenUserHasAdminRole_ShouldReturnAdminRole() {
        // Given
        String expectedToken = "jwt-token-123";
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("admin");
        when(userDetails.getAuthorities()).thenReturn((Collection) authorities);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("admin", result.getUsername());
        assertEquals("ROLE_ADMIN", result.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void getCurrentUser_WhenUserExists_ShouldReturnUser() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        User result = authService.getCurrentUser(username);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void getCurrentUser_WhenUserNotExists_ShouldThrowException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.getCurrentUser(username));
        assertEquals("User not found: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void logout_WhenTokenWithBearerPrefix_ShouldBlacklistTokenWithoutPrefix() {
        // Given
        String tokenWithBearer = "Bearer jwt-token-123";
        String expectedToken = "jwt-token-123";

        // When
        authService.logout(tokenWithBearer);

        // Then
        verify(tokenBlacklistService).blacklistToken(expectedToken);
    }

    @Test
    void logout_WhenTokenWithoutBearerPrefix_ShouldBlacklistTokenAsIs() {
        // Given
        String token = "jwt-token-123";

        // When
        authService.logout(token);

        // Then
        verify(tokenBlacklistService).blacklistToken(token);
    }

    @Test
    void logout_WhenNullToken_ShouldHandleGracefully() {
        // Given
        String nullToken = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> authService.logout(nullToken));
    }

    @Test
    void logout_WhenEmptyToken_ShouldBlacklistEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        authService.logout(emptyToken);

        // Then
        verify(tokenBlacklistService).blacklistToken(emptyToken);
    }

    @Test
    void logout_WhenTokenStartsWithBearerButInvalid_ShouldExtractCorrectly() {
        // Given
        String malformedToken = "Bearer";

        // When
        authService.logout(malformedToken);

        // Then
        verify(tokenBlacklistService).blacklistToken("");
    }

    @Test
    void login_WhenAuthenticationManagerThrowsGenericException_ShouldPropagateException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication service unavailable"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.login(loginRequest));
        assertEquals("Authentication service unavailable", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void login_WhenJwtUtilThrowsException_ShouldPropagateException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenThrow(new RuntimeException("JWT generation failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authService.login(loginRequest));
        assertEquals("JWT generation failed", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
    }
}
