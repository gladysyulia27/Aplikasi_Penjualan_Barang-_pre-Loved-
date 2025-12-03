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

    @Test
    @DisplayName("User PrePersist callback mengatur createdAt dan updatedAt")
    void user_PrePersist_ShouldSetTimestamps() throws Exception {
        User user = new User();
        user.setName("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");

        // Call PrePersist callback using reflection
        java.lang.reflect.Method onCreate = User.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(user);

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    @DisplayName("User PreUpdate callback mengatur updatedAt")
    void user_PreUpdate_ShouldSetUpdatedAt() throws Exception {
        User user = new User();
        user.setName("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        LocalDateTime oldTime = LocalDateTime.now().minusDays(1);
        user.setCreatedAt(oldTime);
        user.setUpdatedAt(oldTime);

        // Call PreUpdate callback using reflection
        java.lang.reflect.Method onUpdate = User.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(user);

        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(oldTime));
        assertEquals(oldTime, user.getCreatedAt()); // createdAt should not change
    }
}

