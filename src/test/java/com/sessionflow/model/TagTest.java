package com.sessionflow.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tag Model Tests")
class TagTest {

    private Validator validator;
    private Tag tag;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        tag = new Tag();
    }

    @Test
    @DisplayName("Should create tag with valid name and color")
    void shouldCreateTagWithValidNameAndColor() {
        // Given
        String name = "工作";
        String color = "#FF5733";
        
        // When
        Tag tag = new Tag(name, color);
        
        // Then
        assertEquals(name, tag.getName());
        assertEquals(color, tag.getColor());
        assertNull(tag.getId());
    }

    @Test
    @DisplayName("Should validate tag name is not blank")
    void shouldValidateTagNameIsNotBlank() {
        // Given
        tag.setName("");
        tag.setColor("#FF5733");
        
        // When
        Set<ConstraintViolation<Tag>> violations = validator.validate(tag);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Tag name cannot be blank")));
    }

    @Test
    @DisplayName("Should validate color format")
    void shouldValidateColorFormat() {
        // Given
        tag.setName("工作");
        tag.setColor("invalid-color");
        
        // When
        Set<ConstraintViolation<Tag>> violations = validator.validate(tag);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Color must be a valid hex color code")));
    }

    @Test
    @DisplayName("Should accept valid hex color codes")
    void shouldAcceptValidHexColorCodes() {
        // Given
        String[] validColors = {"#FF5733", "#33FF57", "#3357FF", "#000000", "#FFFFFF", "#123abc"};
        
        for (String color : validColors) {
            tag.setName("Test");
            tag.setColor(color);
            
            // When
            Set<ConstraintViolation<Tag>> violations = validator.validate(tag);
            
            // Then
            assertTrue(violations.isEmpty(), "Color " + color + " should be valid");
        }
    }

    @Test
    @DisplayName("Should reject invalid hex color codes")
    void shouldRejectInvalidHexColorCodes() {
        // Given
        String[] invalidColors = {"FF5733", "#FF573", "#GG5733", "red", "#FF5733X", ""};
        
        for (String color : invalidColors) {
            tag.setName("Test");
            tag.setColor(color);
            
            // When
            Set<ConstraintViolation<Tag>> violations = validator.validate(tag);
            
            // Then
            assertFalse(violations.isEmpty(), "Color " + color + " should be invalid");
        }
    }

    @Test
    @DisplayName("Should set timestamps on create")
    void shouldSetTimestampsOnCreate() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        tag.setName("Test Tag");
        tag.setColor("#FF5733");
        
        // When
        tag.onCreate();
        
        // Then
        assertNotNull(tag.getCreatedAt());
        assertNotNull(tag.getUpdatedAt());
        assertTrue(tag.getCreatedAt().isAfter(before));
        assertTrue(tag.getUpdatedAt().isAfter(before));
        // Use a small tolerance for timestamp comparison
        assertTrue(Math.abs(Duration.between(tag.getCreatedAt(), tag.getUpdatedAt()).toNanos()) < 1_000_000); // 1ms tolerance
    }

    @Test
    @DisplayName("Should update timestamp on update")
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        // Given
        tag.setName("Test Tag");
        tag.setColor("#FF5733");
        tag.onCreate();
        LocalDateTime originalCreatedAt = tag.getCreatedAt();
        LocalDateTime originalUpdatedAt = tag.getUpdatedAt();
        
        // Wait a bit to ensure different timestamp
        Thread.sleep(10);
        
        // When
        tag.onUpdate();
        
        // Then
        assertEquals(originalCreatedAt, tag.getCreatedAt()); // createdAt should not change
        assertTrue(tag.getUpdatedAt().isAfter(originalUpdatedAt));
    }
} 