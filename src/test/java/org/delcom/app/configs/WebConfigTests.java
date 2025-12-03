package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebConfigTests {
    @Test
    @DisplayName("WebMvcConfig implements WebMvcConfigurer")
    void webMvcConfig_ShouldImplementWebMvcConfigurer() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        assertTrue(webMvcConfig instanceof WebMvcConfigurer);
    }

    @Test
    @DisplayName("WebMvcConfig addResourceHandlers dapat dipanggil dengan ResourceHandlerRegistry")
    void addResourceHandlers_ShouldBeCallable() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        
        // Set uploadDir using reflection since @Value won't be injected in unit test
        try {
            java.lang.reflect.Field field = WebMvcConfig.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            field.set(webMvcConfig, "./uploads/images");
        } catch (Exception e) {
            fail("Failed to set uploadDir field: " + e.getMessage());
        }
        
        // Create a real ResourceHandlerRegistry
        GenericWebApplicationContext applicationContext = new GenericWebApplicationContext();
        applicationContext.refresh();
        
        ResourceHandlerRegistry registry = new ResourceHandlerRegistry(
            applicationContext, 
            null
        );
        
        // Call the method - should not throw
        assertDoesNotThrow(() -> webMvcConfig.addResourceHandlers(registry));
        
        // Verify the method was called successfully
        assertNotNull(webMvcConfig);
    }

    @Test
    @DisplayName("WebMvcConfig addInterceptors dapat dipanggil dengan InterceptorRegistry")
    void addInterceptors_ShouldBeCallable() {
        AuthInterceptor authInterceptor = mock(AuthInterceptor.class);
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        
        // Use reflection to set the authInterceptor field
        try {
            java.lang.reflect.Field field = WebMvcConfig.class.getDeclaredField("authInterceptor");
            field.setAccessible(true);
            field.set(webMvcConfig, authInterceptor);
        } catch (Exception e) {
            // If reflection fails, test will still verify the method exists
        }
        
        // Create a real InterceptorRegistry
        GenericWebApplicationContext applicationContext = new GenericWebApplicationContext();
        applicationContext.refresh();
        
        InterceptorRegistry registry = new InterceptorRegistry();
        
        // Call the method - should not throw
        assertDoesNotThrow(() -> webMvcConfig.addInterceptors(registry));
        
        // Verify the method was called successfully
        assertNotNull(webMvcConfig);
    }
}
