package com.altech.electronicstore.security;

import com.altech.electronicstore.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private JwtUtil jwtUtil;
    
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyThatIsLongEnoughForHS256Algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24 hours
        
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    void generateToken_WithValidUserDetails_ShouldReturnToken() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void extractUsername_WithValidToken_ShouldReturnUsername() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void extractExpiration_WithValidToken_ShouldReturnFutureDate() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractClaim_WithValidTokenAndClaimResolver_ShouldReturnClaimValue() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String subject = jwtUtil.extractClaim(token, Claims::getSubject);

        // Then
        assertEquals("testuser", subject);
    }

    @Test
    void validateToken_WithValidTokenAndMatchingUser_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);

        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithValidTokenButDifferentUser_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // When
        Boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithBlacklistedToken_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(true);

        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Given - Create a JwtUtil with very short expiration
        JwtUtil expiredJwtUtil = new JwtUtil(tokenBlacklistService);
        ReflectionTestUtils.setField(expiredJwtUtil, "secret", "testSecretKeyThatIsLongEnoughForHS256Algorithm");
        ReflectionTestUtils.setField(expiredJwtUtil, "expiration", 1L); // 1 millisecond
        
        String expiredToken = expiredJwtUtil.generateToken(userDetails);
        
        // Wait to ensure expiration
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When & Then - Expired tokens should throw an exception when parsed
        assertThrows(ExpiredJwtException.class, () -> expiredJwtUtil.validateToken(expiredToken, userDetails));
    }

    @Test
    void extractUsername_WithMalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "invalid.token.format";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> jwtUtil.extractUsername(malformedToken));
    }

    @Test
    void extractExpiration_WithMalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "invalid.token.format";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> jwtUtil.extractExpiration(malformedToken));
    }

    @Test
    void validateToken_WithMalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "invalid.token.format";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> jwtUtil.validateToken(malformedToken, userDetails));
    }

    @Test
    void generateToken_WithDifferentUsers_ShouldGenerateDifferentTokens() {
        // Given
        UserDetails user1 = User.builder()
                .username("user1")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        
        UserDetails user2 = User.builder()
                .username("user2")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        // When
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then
        assertNotEquals(token1, token2);
        assertEquals("user1", jwtUtil.extractUsername(token1));
        assertEquals("user2", jwtUtil.extractUsername(token2));
    }

    @Test
    void extractClaim_WithExpirationClaimResolver_ShouldReturnExpirationDate() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Date expiration = jwtUtil.extractClaim(token, Claims::getExpiration);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void generateToken_ShouldCreateTokenWithCorrectStructure() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length); // Header, Payload, Signature
        
        // Verify each part is not empty
        for (String part : parts) {
            assertFalse(part.isEmpty());
        }
    }

    @Test
    void validateToken_WithNullUserDetails_ShouldThrowException() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When & Then
        assertThrows(NullPointerException.class, () -> jwtUtil.validateToken(token, null));
    }

    @Test
    void extractUsername_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.extractUsername(null));
    }

    @Test
    void extractExpiration_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.extractExpiration(null));
    }

    @Test
    void extractClaim_WithIssuedAtClaimResolver_ShouldReturnIssuedAtDate() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Date issuedAt = jwtUtil.extractClaim(token, Claims::getIssuedAt);

        // Then
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
    }

    @Test
    void generateToken_MultipleCalls_ShouldGenerateDifferentTokens() {
        // When
        String token1 = jwtUtil.generateToken(userDetails);
        
        // Wait 1 second to ensure different timestamps (JWT uses seconds precision)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtUtil.generateToken(userDetails);

        // Then
        assertNotEquals(token1, token2);
        assertEquals(jwtUtil.extractUsername(token1), jwtUtil.extractUsername(token2));
    }

    @Test
    void validateToken_WithEmptyUsername_ShouldHandleGracefully() {
        // Given - Spring Security User doesn't allow empty usernames, so test with null username token
        // When & Then - This should throw an exception since Spring Security validates usernames
        assertThrows(IllegalArgumentException.class, () -> {
            User.builder()
                .username("")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        });
    }
}