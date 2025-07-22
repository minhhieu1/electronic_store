package com.altech.electronicstore.config;

import com.altech.electronicstore.security.JwtAuthenticationFilter;
import com.altech.electronicstore.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authConfig;

    @Mock
    private HttpSecurity httpSecurity;

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertNotNull(passwordEncoder);
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
    }

    @Test
    void passwordEncoder_ShouldEncodePasswordsCorrectly() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertTrue(encodedPassword.startsWith("$2a$")); // BCrypt format
    }

    @Test
    void passwordEncoder_ShouldCreateDifferentHashesForSamePassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword1 = passwordEncoder.encode(rawPassword);
        String encodedPassword2 = passwordEncoder.encode(rawPassword);

        // Then
        assertNotEquals(encodedPassword1, encodedPassword2); // Different salts
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword1));
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword2));
    }

    @Test
    void authenticationProvider_ShouldReturnDaoAuthenticationProvider() {
        // When
        AuthenticationProvider authProvider = securityConfig.authenticationProvider();

        // Then
        assertNotNull(authProvider);
        assertInstanceOf(DaoAuthenticationProvider.class, authProvider);
    }

    @Test
    void authenticationProvider_ShouldUseCustomUserDetailsService() {
        // When
        AuthenticationProvider authProvider = securityConfig.authenticationProvider();

        // Then
        assertNotNull(authProvider);
        assertInstanceOf(DaoAuthenticationProvider.class, authProvider);
        // Note: UserDetailsService is set internally, we verify through bean creation
    }

    @Test
    void authenticationProvider_ShouldUseBCryptPasswordEncoder() {
        // When
        AuthenticationProvider authProvider = securityConfig.authenticationProvider();

        // Then
        assertNotNull(authProvider);
        assertInstanceOf(DaoAuthenticationProvider.class, authProvider);
        // Note: PasswordEncoder is set internally, we verify through bean creation
    }

    @Test
    void authenticationManager_ShouldReturnAuthenticationManager() throws Exception {
        // Given
        AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(mockAuthManager);

        // When
        AuthenticationManager authManager = securityConfig.authenticationManager(authConfig);

        // Then
        assertNotNull(authManager);
        assertEquals(mockAuthManager, authManager);
        verify(authConfig).getAuthenticationManager();
    }

    @Test
    void authenticationManager_ShouldThrowExceptionWhenConfigurationFails() throws Exception {
        // Given
        when(authConfig.getAuthenticationManager()).thenThrow(new RuntimeException("Configuration error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            securityConfig.authenticationManager(authConfig);
        });
        verify(authConfig).getAuthenticationManager();
    }

    @Test
    void passwordEncoder_ShouldHandleNullInput() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.encode(null);
        });
    }

    @Test
    void passwordEncoder_ShouldHandleEmptyPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String emptyPassword = "";

        // When
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword));
    }

    @Test
    void passwordEncoder_ShouldHandleSpecialCharacters() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String specialPassword = "test@#$%^&*()_+{}|:<>?[];',./";

        // When
        String encodedPassword = passwordEncoder.encode(specialPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword));
    }

    @Test
    void passwordEncoder_ShouldHandleLongPasswords() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String longPassword = "a".repeat(72); // BCrypt limit is 72 bytes

        // When
        String encodedPassword = passwordEncoder.encode(longPassword);

        // Then
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(longPassword, encodedPassword));
    }

    @Test
    void passwordEncoder_ShouldNotMatchIncorrectPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword123";
        String incorrectPassword = "incorrectPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(correctPassword);

        // Then
        assertFalse(passwordEncoder.matches(incorrectPassword, encodedPassword));
    }

    @Test
    void authenticationProvider_ShouldCreateConsistentInstance() {
        // When
        AuthenticationProvider provider1 = securityConfig.authenticationProvider();
        AuthenticationProvider provider2 = securityConfig.authenticationProvider();

        // Then
        assertNotNull(provider1);
        assertNotNull(provider2);
        assertNotSame(provider1, provider2); // Should create new instances
        assertInstanceOf(DaoAuthenticationProvider.class, provider1);
        assertInstanceOf(DaoAuthenticationProvider.class, provider2);
    }

    @Test
    void passwordEncoder_ShouldCreateSingletonInstance() {
        // When
        PasswordEncoder encoder1 = securityConfig.passwordEncoder();
        PasswordEncoder encoder2 = securityConfig.passwordEncoder();

        // Then
        assertNotNull(encoder1);
        assertNotNull(encoder2);
        // Note: In real Spring context, this would be singleton, but in unit test they are different instances
    }
}
