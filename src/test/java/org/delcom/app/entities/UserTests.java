package org.delcom.app.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTests {
    @Test
    @DisplayName("User constructor tanpa parameter berhasil")
    void user_DefaultConstructor_ShouldCreateUser() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    @DisplayName("User constructor dengan parameter berhasil")
    void user_ConstructorWithParams_ShouldCreateUser() {
        User user = new User("Test User", "test@example.com", "password123");
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
    }

    @Test
    @DisplayName("User getter dan setter berfungsi")
    void user_GettersAndSetters_ShouldWork() {
        User user = new User();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        user.setId(id);
        user.setName("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(id, user.getId());
        assertEquals("Test", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }
}

