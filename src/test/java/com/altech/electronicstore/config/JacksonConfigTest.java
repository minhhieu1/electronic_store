package com.altech.electronicstore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JacksonConfigTest {

    @InjectMocks
    private JacksonConfig jacksonConfig;

    @Test
    void objectMapper_ShouldReturnConfiguredObjectMapper() {
        // When
        ObjectMapper objectMapper = jacksonConfig.objectMapper();

        // Then
        assertNotNull(objectMapper);
        assertFalse(objectMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    void objectMapper_ShouldHaveJavaTimeModuleRegistered() {
        // When
        ObjectMapper objectMapper = jacksonConfig.objectMapper();

        // Then
        assertNotNull(objectMapper);
        // Check for JSR310Module which handles LocalDateTime serialization
        boolean hasJavaTimeModule = objectMapper.getRegisteredModuleIds().stream()
                .anyMatch(id -> id.toString().contains("jsr310") || id.toString().contains("JavaTime"));
        assertTrue(hasJavaTimeModule);
    }

    @Test
    void objectMapper_ShouldSerializeEmptyBeansWithoutFailure() throws Exception {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        EmptyTestBean emptyBean = new EmptyTestBean();

        // When & Then
        assertDoesNotThrow(() -> objectMapper.writeValueAsString(emptyBean));
        String json = objectMapper.writeValueAsString(emptyBean);
        assertEquals("{}", json);
    }

    @Test
    void objectMapper_ShouldSerializeLocalDateTimeAsString() throws Exception {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        LocalDateTime dateTime = LocalDateTime.of(2025, 7, 22, 10, 30, 0);
        Map<String, Object> testData = new HashMap<>();
        testData.put("timestamp", dateTime);

        // When
        String json = objectMapper.writeValueAsString(testData);

        // Then
        assertNotNull(json);
        assertFalse(json.contains("\"timestamp\":" + dateTime.toEpochSecond(java.time.ZoneOffset.UTC))); // Should not be timestamp
        assertTrue(json.contains("2025-07-22T10:30:00")); // Should be ISO format
    }

    @Test
    void objectMapper_ShouldDeserializeLocalDateTime() throws Exception {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        String json = "{\"timestamp\":\"2025-07-22T10:30:00\"}";

        // When
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) objectMapper.readValue(json, Map.class);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("timestamp"));
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void objectMapper_ShouldHandleNullValues() throws Exception {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        Map<String, Object> testData = new HashMap<>();
        testData.put("nullValue", null);
        testData.put("stringValue", "test");

        // When
        String json = objectMapper.writeValueAsString(testData);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"nullValue\":null"));
        assertTrue(json.contains("\"stringValue\":\"test\""));
    }

    @Test
    void objectMapper_ShouldSerializeComplexObject() throws Exception {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        TestBean testBean = new TestBean("testName", 123, LocalDateTime.of(2025, 7, 22, 10, 30, 0));

        // When
        String json = objectMapper.writeValueAsString(testBean);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"testName\""));
        assertTrue(json.contains("\"value\":123"));
        assertTrue(json.contains("2025-07-22T10:30:00"));
    }

    @Test
    void objectMapper_ShouldDeserializeComplexObject() throws Exception {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        String json = "{\"name\":\"testName\",\"value\":123,\"timestamp\":\"2025-07-22T10:30:00\"}";

        // When
        TestBean result = objectMapper.readValue(json, TestBean.class);

        // Then
        assertNotNull(result);
        assertEquals("testName", result.getName());
        assertEquals(123, result.getValue());
        assertNotNull(result.getTimestamp());
    }

    // Test classes for serialization/deserialization testing
    public static class EmptyTestBean {
        // Intentionally empty to test FAIL_ON_EMPTY_BEANS configuration
    }

    public static class TestBean {
        private String name;
        private Integer value;
        private LocalDateTime timestamp;

        public TestBean() {
            // Default constructor for Jackson
        }

        public TestBean(String name, Integer value, LocalDateTime timestamp) {
            this.name = name;
            this.value = value;
            this.timestamp = timestamp;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
