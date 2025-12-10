package org.delcom.app.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstUtilTests {

    @Test
    @DisplayName("ConstUtil memiliki semua constants yang diperlukan")
    void constUtil_ShouldHaveAllConstants() {
        assertNotNull(ConstUtil.KEY_AUTH_TOKEN);
        assertNotNull(ConstUtil.KEY_USER_ID);
        assertNotNull(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN);
        assertNotNull(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER);
        assertNotNull(ConstUtil.TEMPLATE_PAGES_HOME);
        assertNotNull(ConstUtil.TEMPLATE_PAGES_PRODUCTS_DETAIL);
        assertNotNull(ConstUtil.TEMPLATE_PAGES_PRODUCTS_ADD);
        assertNotNull(ConstUtil.TEMPLATE_PAGES_PRODUCTS_EDIT);
        
        assertEquals("AUTH_TOKEN", ConstUtil.KEY_AUTH_TOKEN);
        assertEquals("USER_ID", ConstUtil.KEY_USER_ID);
        assertEquals("pages/auth/login", ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN);
        assertEquals("pages/auth/register", ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER);
        assertEquals("pages/home", ConstUtil.TEMPLATE_PAGES_HOME);
        assertEquals("pages/products/detail", ConstUtil.TEMPLATE_PAGES_PRODUCTS_DETAIL);
        assertEquals("pages/products/add", ConstUtil.TEMPLATE_PAGES_PRODUCTS_ADD);
        assertEquals("pages/products/edit", ConstUtil.TEMPLATE_PAGES_PRODUCTS_EDIT);
    }

    @Test
    @DisplayName("ConstUtil constructor dapat dipanggil")
    void constUtil_Constructor_ShouldBeCallable() {
        // Test default constructor untuk coverage
        ConstUtil constUtil = new ConstUtil();
        assertNotNull(constUtil);
    }
}

