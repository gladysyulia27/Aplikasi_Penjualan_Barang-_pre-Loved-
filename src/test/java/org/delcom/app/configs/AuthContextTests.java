package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthContextTests {
    private AuthContext authContext;

    @BeforeEach
    void setUp() {
        authContext = new AuthContext();
    }

    @Test
    @DisplayName("AuthContext getAuthUser mengembalikan null saat belum di-set")
    void getAuthUser_WhenNotSet_ShouldReturnNull() {
        assertNull(authContext.getAuthUser());
    }

    @Test
    @DisplayName("AuthContext setAuthUser dan getAuthUser berhasil")
    void setAndGetAuthUser_ShouldWork() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@example.com");

        authContext.setAuthUser(user);

        assertNotNull(authContext.getAuthUser());
        assertEquals(user.getId(), authContext.getAuthUser().getId());
        assertEquals(user.getName(), authContext.getAuthUser().getName());
        assertEquals(user.getEmail(), authContext.getAuthUser().getEmail());
    }

    @Test
    @DisplayName("AuthContext isAuthenticated mengembalikan false saat user null")
    void isAuthenticated_WhenUserNull_ShouldReturnFalse() {
        assertFalse(authContext.isAuthenticated());
    }

    @Test
    @DisplayName("AuthContext isAuthenticated mengembalikan true saat user di-set")
    void isAuthenticated_WhenUserSet_ShouldReturnTrue() {
        User user = new User();
        user.setId(UUID.randomUUID());

        authContext.setAuthUser(user);

        assertTrue(authContext.isAuthenticated());
    }
}

