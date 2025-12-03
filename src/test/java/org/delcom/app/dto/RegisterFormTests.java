package org.delcom.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterFormTests {

    @Test
    @DisplayName("RegisterForm constructor berhasil membuat instance")
    void constructor_ShouldCreateInstance() {
        RegisterForm form = new RegisterForm();
        assertNotNull(form);
    }

    @Test
    @DisplayName("RegisterForm getter dan setter name berhasil")
    void name_GetterAndSetter_ShouldWork() {
        RegisterForm form = new RegisterForm();
        form.setName("Test User");
        assertEquals("Test User", form.getName());
    }

    @Test
    @DisplayName("RegisterForm getter dan setter email berhasil")
    void email_GetterAndSetter_ShouldWork() {
        RegisterForm form = new RegisterForm();
        form.setEmail("test@example.com");
        assertEquals("test@example.com", form.getEmail());
    }

    @Test
    @DisplayName("RegisterForm getter dan setter password berhasil")
    void password_GetterAndSetter_ShouldWork() {
        RegisterForm form = new RegisterForm();
        form.setPassword("password123");
        assertEquals("password123", form.getPassword());
    }
}

