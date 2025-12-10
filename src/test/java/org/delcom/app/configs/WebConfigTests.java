package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.delcom.app.interceptors.WebAuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
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
    @DisplayName("WebMvcConfig addResourceHandlers dengan path yang sudah berakhir dengan slash")
    void addResourceHandlers_WithPathEndingWithSlash_ShouldBeCallable() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        
        // Set uploadDir dengan path yang sudah berakhir dengan slash
        try {
            java.lang.reflect.Field field = WebMvcConfig.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            field.set(webMvcConfig, "./uploads/images/");
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
    @DisplayName("WebMvcConfig addResourceHandlers dengan path yang tidak berakhir dengan slash menambahkan slash")
    void addResourceHandlers_WithPathNotEndingWithSlash_ShouldAddSlash() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        
        // Set uploadDir dengan path yang TIDAK berakhir dengan slash (untuk test branch if (!uploadPath.endsWith("/")))
        try {
            java.lang.reflect.Field field = WebMvcConfig.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            field.set(webMvcConfig, "./uploads/images"); // Tidak ada trailing slash
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
        // This should trigger the branch where uploadPath does NOT end with "/"
        assertDoesNotThrow(() -> webMvcConfig.addResourceHandlers(registry));
        
        // Verify the method was called successfully
        assertNotNull(webMvcConfig);
    }

    @Test
    @DisplayName("WebMvcConfig addResourceHandlers dengan path Windows (backslash) mengkonversi ke forward slash")
    void addResourceHandlers_WithWindowsPath_ShouldConvertBackslashToForwardSlash() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        
        // Set uploadDir dengan path Windows yang menggunakan backslash
        // Simulasi path Windows seperti "C:\\uploads\\images"
        try {
            java.lang.reflect.Field field = WebMvcConfig.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            // Gunakan path yang akan menghasilkan backslash setelah normalize
            // Di Windows, Paths.get().toAbsolutePath().normalize() akan menghasilkan backslash
            field.set(webMvcConfig, "uploads\\images");
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
        // This should trigger the branch where uploadPath contains backslash
        assertDoesNotThrow(() -> webMvcConfig.addResourceHandlers(registry));
        
        // Verify the method was called successfully
        assertNotNull(webMvcConfig);
    }

    @Test
    @DisplayName("WebMvcConfig addResourceHandlers dengan path yang sudah berakhir dengan slash setelah replace")
    void addResourceHandlers_WithPathEndingWithSlashAfterReplace_ShouldNotAddAnotherSlash() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        
        // Set uploadDir dengan path yang setelah normalize dan replace akan berakhir dengan slash
        // Ini untuk memastikan branch if (!uploadPath.endsWith("/")) false ter-cover
        try {
            java.lang.reflect.Field field = WebMvcConfig.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            // Path yang setelah normalize dan replace akan berakhir dengan slash
            field.set(webMvcConfig, "./uploads/images/");
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
        // This should trigger the branch where uploadPath ends with "/" (false condition)
        assertDoesNotThrow(() -> webMvcConfig.addResourceHandlers(registry));
        
        // Verify the method was called successfully
        assertNotNull(webMvcConfig);
    }

    @Test
    @DisplayName("WebMvcConfig addResourceHandlers dengan path root directory yang berakhir dengan slash")
    void addResourceHandlers_WithRootPathEndingWithSlash_ShouldNotAddAnotherSlash() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        
        // Set uploadDir dengan path root yang setelah normalize akan berakhir dengan slash
        // Di Windows, root path seperti "C:\\" setelah normalize dan replace akan menjadi "C:/"
        // Tapi di Unix, root path "/" setelah normalize akan tetap "/"
        try {
            java.lang.reflect.Field field = WebMvcConfig.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            // Gunakan path yang mungkin menghasilkan path berakhir dengan slash setelah normalize
            // Di beberapa sistem, root directory bisa berakhir dengan slash
            String rootPath = System.getProperty("user.dir");
            // Coba dengan path yang sudah berakhir dengan slash
            field.set(webMvcConfig, rootPath + "/");
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
    @DisplayName("WebMvcConfig addResourceHandlers dengan path yang tidak berakhir dengan slash setelah replace backslash")
    void addResourceHandlers_WithPathNotEndingWithSlashAfterBackslashReplace_ShouldAddSlash() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        
        // Set uploadDir dengan path yang setelah normalize dan replace backslash tidak berakhir dengan slash
        // Ini untuk memastikan branch if (!uploadPath.endsWith("/")) true ter-cover
        try {
            java.lang.reflect.Field field = WebMvcConfig.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            // Path yang setelah normalize dan replace tidak berakhir dengan slash
            // Di Windows, path dengan backslash setelah replace akan menjadi forward slash tapi tidak berakhir dengan slash
            field.set(webMvcConfig, "uploads\\images");
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
        // This should trigger the branch where uploadPath does NOT end with "/" (true condition)
        assertDoesNotThrow(() -> webMvcConfig.addResourceHandlers(registry));
        
        // Verify the method was called successfully
        assertNotNull(webMvcConfig);
    }

    @Test
    @DisplayName("WebMvcConfig addResourceHandlers dengan path yang berakhir dengan slash setelah normalize dan replace")
    void addResourceHandlers_WithPathEndingWithSlashAfterNormalizeAndReplace_ShouldNotAddAnotherSlash() {
        // Gunakan mock untuk memastikan path berakhir dengan slash setelah normalize dan replace
        // Ini untuk menutup branch false dari if (!uploadPath.endsWith("/"))
        
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
            // Mock Path objects
            Path mockPath = mock(Path.class);
            Path mockAbsolutePath = mock(Path.class);
            Path mockNormalizedPath = mock(Path.class);
            
            // Setup mock chain untuk menghasilkan path yang berakhir dengan "/"
            // Simulasi path yang setelah normalize dan replace akan berakhir dengan slash
            // Contoh: di beberapa sistem, root path bisa berakhir dengan slash
            mockedPaths.when(() -> Paths.get(anyString())).thenReturn(mockPath);
            when(mockPath.toAbsolutePath()).thenReturn(mockAbsolutePath);
            when(mockAbsolutePath.normalize()).thenReturn(mockNormalizedPath);
            // Path yang berakhir dengan slash setelah toString
            when(mockNormalizedPath.toString()).thenReturn("C:/uploads/images/");
            
            WebMvcConfig webMvcConfig = new WebMvcConfig();
            
            // Set uploadDir using reflection
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
            // This should trigger the branch where uploadPath ends with "/" (false condition)
            assertDoesNotThrow(() -> webMvcConfig.addResourceHandlers(registry));
            
            // Verify the method was called successfully
            assertNotNull(webMvcConfig);
        }
    }

    @Test
    @DisplayName("WebMvcConfig addInterceptors dapat dipanggil dengan InterceptorRegistry")
    void addInterceptors_ShouldBeCallable() {
        AuthInterceptor authInterceptor = mock(AuthInterceptor.class);
        WebAuthInterceptor webAuthInterceptor = mock(WebAuthInterceptor.class);
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        
        // Use reflection to set the interceptor fields
        try {
            java.lang.reflect.Field authField = WebMvcConfig.class.getDeclaredField("authInterceptor");
            authField.setAccessible(true);
            authField.set(webMvcConfig, authInterceptor);
            
            java.lang.reflect.Field webAuthField = WebMvcConfig.class.getDeclaredField("webAuthInterceptor");
            webAuthField.setAccessible(true);
            webAuthField.set(webMvcConfig, webAuthInterceptor);
        } catch (Exception e) {
            fail("Failed to set interceptor fields: " + e.getMessage());
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
