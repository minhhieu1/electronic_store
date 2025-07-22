package com.altech.electronicstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    private String testToken;

    @BeforeEach
    void setUp() {
        testToken = "test.jwt.token.123";
    }

    @Test
    void blacklistToken_WhenValidToken_ShouldAddToBlacklist() {
        // Given
        String token = testToken;

        // When
        tokenBlacklistService.blacklistToken(token);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
    }

    @Test
    void blacklistToken_WhenNullToken_ShouldThrowException() {
        // Given
        String token = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> tokenBlacklistService.blacklistToken(token));
    }

    @Test
    void blacklistToken_WhenEmptyToken_ShouldAddEmptyToBlacklist() {
        // Given
        String token = "";

        // When
        tokenBlacklistService.blacklistToken(token);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(""));
    }

    @Test
    void blacklistToken_WhenDuplicateToken_ShouldNotDuplicate() {
        // Given
        String token = testToken;

        // When
        tokenBlacklistService.blacklistToken(token);
        tokenBlacklistService.blacklistToken(token); // Add same token again

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
        // Since it's a Set, it shouldn't have duplicates, but we can't easily test that
        // The important thing is that it still returns true
    }

    @Test
    void blacklistToken_WhenMultipleTokens_ShouldAddAllToBlacklist() {
        // Given
        String token1 = "token1.jwt.123";
        String token2 = "token2.jwt.456";
        String token3 = "token3.jwt.789";

        // When
        tokenBlacklistService.blacklistToken(token1);
        tokenBlacklistService.blacklistToken(token2);
        tokenBlacklistService.blacklistToken(token3);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token1));
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token2));
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token3));
    }

    @Test
    void isTokenBlacklisted_WhenTokenNotBlacklisted_ShouldReturnFalse() {
        // Given
        String token = "non.blacklisted.token";

        // When
        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        // Then
        assertFalse(result);
    }

    @Test
    void isTokenBlacklisted_WhenTokenBlacklisted_ShouldReturnTrue() {
        // Given
        String token = testToken;
        tokenBlacklistService.blacklistToken(token);

        // When
        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        // Then
        assertTrue(result);
    }

    @Test
    void isTokenBlacklisted_WhenNullToken_ShouldThrowException() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> tokenBlacklistService.isTokenBlacklisted(null));
    }

    @Test
    void blacklistToken_WhenValidToken_ShouldNotAllowNullChecks() {
        // Given
        String token = testToken;
        tokenBlacklistService.blacklistToken(token);

        // When & Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
        
        // Null operations should throw exceptions
        assertThrows(NullPointerException.class, () -> tokenBlacklistService.isTokenBlacklisted(null));
        assertThrows(NullPointerException.class, () -> tokenBlacklistService.blacklistToken(null));
    }

    @Test
    void isTokenBlacklisted_WhenEmptyToken_ShouldReturnFalse() {
        // Given & When
        boolean result = tokenBlacklistService.isTokenBlacklisted("");

        // Then
        assertFalse(result);
    }

    @Test
    void isTokenBlacklisted_WhenEmptyTokenBlacklisted_ShouldReturnTrue() {
        // Given
        tokenBlacklistService.blacklistToken("");

        // When
        boolean result = tokenBlacklistService.isTokenBlacklisted("");

        // Then
        assertTrue(result);
    }

    @Test
    void tokenBlacklist_WhenCaseSensitive_ShouldBeCaseSensitive() {
        // Given
        String lowerCaseToken = "test.token.lowercase";
        String upperCaseToken = "TEST.TOKEN.LOWERCASE";

        // When
        tokenBlacklistService.blacklistToken(lowerCaseToken);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(lowerCaseToken));
        assertFalse(tokenBlacklistService.isTokenBlacklisted(upperCaseToken));
    }

    @Test
    void tokenBlacklist_WhenWhitespaceTokens_ShouldHandleCorrectly() {
        // Given
        String tokenWithSpaces = " token with spaces ";
        String trimmedToken = "token with spaces";

        // When
        tokenBlacklistService.blacklistToken(tokenWithSpaces);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(tokenWithSpaces));
        assertFalse(tokenBlacklistService.isTokenBlacklisted(trimmedToken));
    }

    @Test
    void tokenBlacklist_WhenLongToken_ShouldHandleCorrectly() {
        // Given
        StringBuilder longTokenBuilder = new StringBuilder("very.long.token.");
        for (int i = 0; i < 1000; i++) {
            longTokenBuilder.append("x");
        }
        String longToken = longTokenBuilder.toString();

        // When
        tokenBlacklistService.blacklistToken(longToken);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(longToken));
    }

    @Test
    void tokenBlacklist_WhenSpecialCharacters_ShouldHandleCorrectly() {
        // Given
        String tokenWithSpecialChars = "token!@#$%^&*()_+-=[]{}|;':\",./<>?";

        // When
        tokenBlacklistService.blacklistToken(tokenWithSpecialChars);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(tokenWithSpecialChars));
    }

    @Test
    void tokenBlacklist_ConcurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Given
        String baseToken = "concurrent.token.";
        int numberOfThreads = 10;
        int tokensPerThread = 100;
        Thread[] threads = new Thread[numberOfThreads];

        // When - Create multiple threads that add tokens concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < tokensPerThread; j++) {
                    String token = baseToken + threadId + "." + j;
                    tokenBlacklistService.blacklistToken(token);
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - Verify all tokens were added correctly
        for (int i = 0; i < numberOfThreads; i++) {
            for (int j = 0; j < tokensPerThread; j++) {
                String token = baseToken + i + "." + j;
                assertTrue(tokenBlacklistService.isTokenBlacklisted(token),
                        "Token should be blacklisted: " + token);
            }
        }
    }
}
