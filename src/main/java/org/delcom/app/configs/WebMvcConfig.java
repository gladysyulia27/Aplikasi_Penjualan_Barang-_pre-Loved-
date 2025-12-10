package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.delcom.app.interceptors.WebAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Autowired
    private WebAuthInterceptor webAuthInterceptor;
    
    @Value("${app.upload.dir:./uploads/images}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve file dari local storage menggunakan path absolut
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        // Convert backslash ke forward slash untuk file: protocol (Windows compatibility)
        uploadPath = uploadPath.replace("\\", "/");
        // Pastikan path berakhir dengan slash
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }
        
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor untuk API endpoints (menggunakan Bearer token)
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**") // Terapkan ke semua endpoint /api
                .excludePathPatterns("/api/auth/**") // Kecuali endpoint auth
                .excludePathPatterns("/api/public/**"); // Dan endpoint public
        
        // Interceptor untuk Web pages (menggunakan cookie token)
        registry.addInterceptor(webAuthInterceptor)
                .addPathPatterns("/**") // Terapkan ke semua halaman
                .excludePathPatterns("/auth/**") // Kecuali halaman auth
                .excludePathPatterns("/api/**") // Kecuali API (sudah dihandle AuthInterceptor)
                .excludePathPatterns("/static/**") // Kecuali static resources
                .excludePathPatterns("/css/**") // Kecuali CSS
                .excludePathPatterns("/js/**") // Kecuali JavaScript
                .excludePathPatterns("/images/**") // Kecuali images
                .excludePathPatterns("/uploads/**") // Kecuali uploaded files
                .excludePathPatterns("/error") // Kecuali error page
                .excludePathPatterns("/favicon.ico"); // Kecuali favicon
    }
}

