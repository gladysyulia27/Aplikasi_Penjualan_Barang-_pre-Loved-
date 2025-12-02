package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTests {
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
    }

    @Test
    @DisplayName("Store file dengan file valid berhasil")
    void storeFile_WithValidFile_ShouldReturnPath() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        String result = fileStorageService.storeFile(file);

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/images/"));
        assertTrue(result.endsWith(".jpg"));
    }

    @Test
    @DisplayName("Store file kosong throw exception")
    void storeFile_WithEmptyFile_ShouldThrowException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    @DisplayName("Delete file dengan URL valid berhasil")
    void deleteFile_WithValidUrl_ShouldNotThrowException() {
        // Test tidak throw exception meskipun file tidak ada
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile("/uploads/images/nonexistent.jpg");
        });
    }

    @Test
    @DisplayName("Delete file dengan URL null tidak throw exception")
    void deleteFile_WithNullUrl_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile(null);
        });
    }

    @Test
    @DisplayName("Delete file dengan URL kosong tidak throw exception")
    void deleteFile_WithEmptyUrl_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile("");
        });
    }
}

