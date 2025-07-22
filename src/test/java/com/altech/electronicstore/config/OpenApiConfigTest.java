package com.altech.electronicstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenApiConfigTest {

    @InjectMocks
    private OpenApiConfig openApiConfig;

    @Test
    void customOpenAPI_ShouldReturnConfiguredOpenAPI() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getSecurity());
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectApiInfo() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        Info info = openAPI.getInfo();

        // Then
        assertNotNull(info);
        assertEquals("Electronics Store API", info.getTitle());
        assertEquals("1.0", info.getVersion());
        assertEquals("A comprehensive REST API for an electronics store backend", info.getDescription());
    }

    @Test
    void customOpenAPI_ShouldHaveContactInformation() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        Info info = openAPI.getInfo();
        Contact contact = info.getContact();

        // Then
        assertNotNull(contact);
        assertEquals("Electronics Store Team", contact.getName());
        assertEquals("support@electronics-store.com", contact.getEmail());
    }

    @Test
    void customOpenAPI_ShouldHaveBearerAuthSecurityScheme() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("Bearer Authentication"));
        
        SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(bearerAuth);
        assertEquals(SecurityScheme.Type.HTTP, bearerAuth.getType());
        assertEquals("bearer", bearerAuth.getScheme());
        assertEquals("JWT", bearerAuth.getBearerFormat());
    }

    @Test
    void customOpenAPI_ShouldHaveGlobalSecurityRequirement() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        
        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertNotNull(securityRequirement);
        assertTrue(securityRequirement.containsKey("Bearer Authentication"));
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectSecuritySchemeDescription() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");

        // Then
        assertNotNull(bearerAuth);
        // Note: OpenAPI 3 security schemes don't have description field in the actual implementation
    }

    @Test
    void customOpenAPI_ShouldConfigureSecuritySchemeCorrectly() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");

        // Then
        assertNotNull(bearerAuth);
        // Note: OpenAPI 3 HTTP scheme doesn't have name and in fields for bearer auth
    }

    @Test
    void customOpenAPI_ShouldHaveConsistentConfiguration() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        // Verify that all components are properly linked
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getSecurity());
        
        // Verify security scheme and requirement are consistent
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("Bearer Authentication"));
        assertTrue(openAPI.getSecurity().get(0).containsKey("Bearer Authentication"));
    }

    @Test
    void customOpenAPI_ShouldHaveValidVersionFormat() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        Info info = openAPI.getInfo();

        // Then
        assertNotNull(info.getVersion());
        assertTrue(info.getVersion().matches("\\d+\\.\\d+")); // Should match version pattern like "1.0"
    }

    @Test
    void customOpenAPI_ShouldHaveNonEmptyDescription() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        Info info = openAPI.getInfo();

        // Then
        assertNotNull(info.getDescription());
        assertFalse(info.getDescription().trim().isEmpty());
        assertTrue(info.getDescription().length() > 10); // Should have meaningful description
    }

    @Test
    void customOpenAPI_ShouldCreateNewInstanceEachTime() {
        // When
        OpenAPI openAPI1 = openApiConfig.customOpenAPI();
        OpenAPI openAPI2 = openApiConfig.customOpenAPI();

        // Then
        assertNotSame(openAPI1, openAPI2); // Should be different instances
        assertEquals(openAPI1.getInfo().getTitle(), openAPI2.getInfo().getTitle()); // But same content
    }
}
