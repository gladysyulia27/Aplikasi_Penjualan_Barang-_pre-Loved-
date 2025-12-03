package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;

class WebConfigTests {
    @Test
    @DisplayName("WebConfig implements WebMvcConfigurer")
    void webConfig_ShouldImplementWebMvcConfigurer() {
        WebConfig webConfig = new WebConfig();
        assertTrue(webConfig instanceof WebMvcConfigurer);
    }

    @Test
    @DisplayName("WebConfig addResourceHandlers dapat dipanggil dengan ResourceHandlerRegistry")
    void addResourceHandlers_ShouldBeCallable() {
        WebConfig webConfig = new WebConfig();
        
        // Create a real ResourceHandlerRegistry
        GenericWebApplicationContext applicationContext = new GenericWebApplicationContext();
        applicationContext.refresh();
        
        ResourceHandlerRegistry registry = new ResourceHandlerRegistry(
            applicationContext, 
            null
        );
        
        // Call the method - should not throw
        assertDoesNotThrow(() -> webConfig.addResourceHandlers(registry));
        
        // Verify the method was called successfully
        assertNotNull(webConfig);
    }
}
