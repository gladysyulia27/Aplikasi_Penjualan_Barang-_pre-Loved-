package org.delcom.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginFormTests {

    @Test
    @DisplayName("LoginForm constructor berhasil membuat instance")
    void constructor_ShouldCreateInstance() {
        LoginForm form = new LoginForm();
        assertNotNull(form);
    }

    @Test
    @DisplayName("LoginForm getter dan setter email berhasil")
    void email_GetterAndSetter_ShouldWork() {
        LoginForm form = new LoginForm();
        form.setEmail("test@example.com");
        assertEquals("test@example.com", form.getEmail());
    }

    @Test
    @DisplayName("LoginForm getter dan setter password berhasil")
    void password_GetterAndSetter_ShouldWork() {
        LoginForm form = new LoginForm();
        form.setPassword("password123");
        assertEquals("password123", form.getPassword());
    }

    @Test
    @DisplayName("LoginForm getter dan setter rememberMe berhasil")
    void rememberMe_GetterAndSetter_ShouldWork() {
        LoginForm form = new LoginForm();
        form.setRememberMe(true);
        assertTrue(form.isRememberMe());
        
        form.setRememberMe(false);
        assertFalse(form.isRememberMe());
    }
}

