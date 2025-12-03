package org.delcom.app.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenTests {
    @Test
    @DisplayName("AuthToken constructor tanpa parameter berhasil")
    void authToken_DefaultConstructor_ShouldCreateAuthToken() {
        AuthToken authToken = new AuthToken();
        assertNotNull(authToken);
    }

    @Test
    @DisplayName("AuthToken constructor dengan parameter berhasil")
    void authToken_ConstructorWithParams_ShouldCreateAuthToken() {
        UUID userId = UUID.randomUUID();
        AuthToken authToken = new AuthToken("test-token", userId);
        assertEquals("test-token", authToken.getToken());
        assertEquals(userId, authToken.getUserId());
    }

    @Test
    @DisplayName("AuthToken getter dan setter berfungsi")
    void authToken_GettersAndSetters_ShouldWork() {
        AuthToken authToken = new AuthToken();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        authToken.setId(id);
        authToken.setToken("token");
        authToken.setUserId(userId);
        authToken.setCreatedAt(now);

        assertEquals(id, authToken.getId());
        assertEquals("token", authToken.getToken());
        assertEquals(userId, authToken.getUserId());
        assertEquals(now, authToken.getCreatedAt());
    }

    @Test
    @DisplayName("AuthToken PrePersist callback mengatur createdAt")
    void authToken_PrePersist_ShouldSetCreatedAt() throws Exception {
        AuthToken authToken = new AuthToken();
        authToken.setToken("test-token");
        authToken.setUserId(UUID.randomUUID());

        // Call PrePersist callback using reflection
        java.lang.reflect.Method onCreate = AuthToken.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(authToken);

        assertNotNull(authToken.getCreatedAt());
    }
}

