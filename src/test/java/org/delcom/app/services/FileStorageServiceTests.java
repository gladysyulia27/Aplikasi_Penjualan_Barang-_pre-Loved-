package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTests {
    private FileStorageService fileStorageService;
    private static final String TEST_UPLOAD_DIR = "./test-uploads/images";

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(TEST_UPLOAD_DIR);
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

    @Test
    @DisplayName("Store file dengan filename tanpa extension berhasil")
    void storeFile_WithNoExtension_ShouldReturnPath() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("testfile");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        String result = fileStorageService.storeFile(file);

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/images/"));
        assertFalse(result.contains("."));
    }

    @Test
    @DisplayName("Store file dengan filename null berhasil")
    void storeFile_WithNullFilename_ShouldReturnPath() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        String result = fileStorageService.storeFile(file);

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/images/"));
    }

    @Test
    @DisplayName("Store file dengan IOException throw exception")
    void storeFile_WithIOException_ShouldThrowException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getInputStream()).thenThrow(new IOException("IO Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(file);
        });
        
        // Message sekarang include error detail
        assertTrue(exception.getMessage().startsWith("Gagal menyimpan file"), 
                   "Message should start with 'Gagal menyimpan file', but was: " + exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    @DisplayName("Delete file dengan URL tanpa leading slash berhasil")
    void deleteFile_WithUrlWithoutLeadingSlash_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile("uploads/images/test.jpg");
        });
    }

    @Test
    @DisplayName("Delete file dengan file yang benar-benar ada berhasil")
    void deleteFile_WithExistingFile_ShouldDeleteFile() throws IOException {
        // Create a temporary file in test upload directory
        java.nio.file.Path uploadDir = java.nio.file.Paths.get(TEST_UPLOAD_DIR).toAbsolutePath().normalize();
        if (!java.nio.file.Files.exists(uploadDir)) {
            java.nio.file.Files.createDirectories(uploadDir);
        }
        
        java.nio.file.Path tempFile = uploadDir.resolve("test-delete-" + System.currentTimeMillis() + ".jpg");
        java.nio.file.Files.createFile(tempFile);
        
        String fileUrl = "/uploads/images/" + tempFile.getFileName().toString();
        
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile(fileUrl);
        });
        
        // Verify file is deleted
        assertFalse(java.nio.file.Files.exists(tempFile), "File should be deleted");
    }

    @Test
    @DisplayName("Delete file dengan IOException tidak throw exception")
    void deleteFile_WithIOException_ShouldNotThrowException() throws IOException {
        // Use Mockito static mocking to simulate IOException during Files.delete()
        try (var mockedFiles = mockStatic(Files.class)) {
            Path testPath = Paths.get("uploads/images/test-file.jpg");
            
            // Mock Files.exists to return true
            mockedFiles.when(() -> Files.exists(testPath)).thenReturn(true);
            // Mock Files.delete to throw IOException
            mockedFiles.when(() -> Files.delete(testPath)).thenThrow(new IOException("Cannot delete file"));
            
            // The method should catch IOException and not throw
            assertDoesNotThrow(() -> {
                fileStorageService.deleteFile("/uploads/images/test-file.jpg");
            });
        }
    }

    @Test
    @DisplayName("FileStorageService constructor berhasil membuat direktori")
    void constructor_ShouldCreateDirectory() {
        // Constructor is called in setUp, so we just verify the service was created
        assertNotNull(fileStorageService);
        // Verify directory exists (it should be created by constructor)
        Path testDir = Paths.get(TEST_UPLOAD_DIR).toAbsolutePath().normalize();
        assertTrue(java.nio.file.Files.exists(testDir));
    }

    @Test
    @DisplayName("FileStorageService constructor membuat direktori ketika tidak ada")
    void constructor_ShouldCreateDirectoryWhenNotExists() throws IOException {
        // Use a unique test directory for this test
        String uniqueTestDir = "./test-uploads-unique-" + System.currentTimeMillis() + "/images";
        
        // Delete directory if it exists to test the createDirectories path
        Path uploadDir = Paths.get(uniqueTestDir).toAbsolutePath().normalize();
        if (Files.exists(uploadDir)) {
            // Delete all files in directory first
            Files.walk(uploadDir)
                .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore errors during cleanup
                    }
                });
        }
        
        // Now create a new instance - this should call Files.createDirectories
        FileStorageService newService = new FileStorageService(uniqueTestDir);
        assertNotNull(newService);
        
        // Verify directory was created
        assertTrue(Files.exists(uploadDir));
        
        // Cleanup
        try {
            Files.walk(uploadDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore errors during cleanup
                    }
                });
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }

    @Test
    @DisplayName("FileStorageService constructor dengan IOException throw exception")
    void constructor_WithIOException_ShouldThrowException() {
        // Use Mockito static mocking to simulate IOException during Files.createDirectories()
        try (var mockedFiles = mockStatic(Files.class);
             var mockedPaths = mockStatic(Paths.class)) {
            
            Path uploadPath = mock(Path.class);
            Path absolutePath = mock(Path.class);
            
            // Mock Paths.get to return our mocked Path
            mockedPaths.when(() -> Paths.get(TEST_UPLOAD_DIR)).thenReturn(uploadPath);
            // Mock toAbsolutePath and normalize
            when(uploadPath.toAbsolutePath()).thenReturn(absolutePath);
            when(absolutePath.normalize()).thenReturn(absolutePath);
            
            // Mock Files.exists to return false (directory doesn't exist)
            mockedFiles.when(() -> Files.exists(absolutePath)).thenReturn(false);
            // Mock Files.createDirectories to throw IOException
            mockedFiles.when(() -> Files.createDirectories(absolutePath)).thenThrow(new IOException("Cannot create directory"));
            
            // Constructor should throw RuntimeException when IOException occurs
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                new FileStorageService(TEST_UPLOAD_DIR);
            });
            
            assertTrue(exception.getMessage().contains("Tidak dapat membuat direktori upload"));
            assertNotNull(exception.getCause());
            assertTrue(exception.getCause() instanceof IOException);
        }
    }
}

