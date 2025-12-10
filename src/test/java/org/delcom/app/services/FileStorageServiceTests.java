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
import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("Store file null throw exception")
    void storeFile_WithNullFile_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(null);
        });
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
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        // Use Mockito static mocking to simulate IOException during Files.copy()
        try (var mockedFiles = mockStatic(Files.class)) {
            // Mock Files.copy to throw IOException
            mockedFiles.when(() -> Files.copy(any(java.io.InputStream.class), any(Path.class), any(java.nio.file.CopyOption.class)))
                .thenThrow(new IOException("Cannot copy file"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                fileStorageService.storeFile(file);
            });
            
            // Message sekarang include error detail
            assertTrue(exception.getMessage().startsWith("Gagal menyimpan file"), 
                       "Message should start with 'Gagal menyimpan file', but was: " + exception.getMessage());
            assertNotNull(exception.getCause());
            assertTrue(exception.getCause() instanceof IOException);
        }
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
            // Get the actual upload path from the service using reflection
            java.lang.reflect.Field uploadPathField = FileStorageService.class.getDeclaredField("uploadPath");
            uploadPathField.setAccessible(true);
            Path actualUploadPath = (Path) uploadPathField.get(fileStorageService);
            Path testFilePath = actualUploadPath.resolve("test-file.jpg");
            
            // Mock Files.exists to return true (file exists)
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenAnswer(invocation -> {
                Path path = invocation.getArgument(0);
                return path.equals(testFilePath);
            });
            
            // Mock Files.delete to throw IOException
            mockedFiles.when(() -> Files.delete(any(Path.class))).thenAnswer(invocation -> {
                Path path = invocation.getArgument(0);
                if (path.equals(testFilePath)) {
                    throw new IOException("Cannot delete file");
                }
                return null;
            });
            
            // The method should catch IOException and not throw
            assertDoesNotThrow(() -> {
                fileStorageService.deleteFile("/uploads/images/test-file.jpg");
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access uploadPath field: " + e.getMessage());
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
    @DisplayName("FileStorageService constructor dengan direktori sudah ada tidak membuat ulang")
    void constructor_WithExistingDirectory_ShouldNotCreateAgain() throws IOException {
        // Use a unique test directory that we'll create first
        String uniqueTestDir = "./test-uploads-existing-" + System.currentTimeMillis() + "/images";
        Path uploadDir = Paths.get(uniqueTestDir).toAbsolutePath().normalize();
        
        // Create directory first to test the branch where Files.exists returns true
        Files.createDirectories(uploadDir);
        assertTrue(Files.exists(uploadDir), "Directory should exist before constructor call");
        
        // Now create a new instance - this should NOT call Files.createDirectories
        // because directory already exists (Files.exists returns true)
        // The constructor should skip the createDirectories call
        FileStorageService newService = new FileStorageService(uniqueTestDir);
        assertNotNull(newService);
        
        // Verify directory still exists (wasn't deleted or recreated)
        assertTrue(Files.exists(uploadDir), "Directory should still exist after constructor");
        
        // Cleanup
        try {
            if (Files.exists(uploadDir)) {
                Files.walk(uploadDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
            }
        } catch (IOException e) {
            // Ignore cleanup errors
        }
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
            mockedPaths.when(() -> Paths.get(anyString())).thenAnswer(invocation -> {
                String dir = invocation.getArgument(0);
                if (dir.equals(TEST_UPLOAD_DIR)) {
                    return uploadPath;
                }
                return Paths.get(dir); // Fallback to real Paths.get for other directories
            });
            
            // Mock toAbsolutePath and normalize
            when(uploadPath.toAbsolutePath()).thenReturn(absolutePath);
            when(absolutePath.normalize()).thenReturn(absolutePath);
            
            // Mock Files.exists to return false (directory doesn't exist)
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenAnswer(invocation -> {
                Path path = invocation.getArgument(0);
                if (path.equals(absolutePath)) {
                    return false; // Directory doesn't exist
                }
                return Files.exists(path); // Fallback to real Files.exists for other paths
            });
            
            // Mock Files.createDirectories to throw IOException
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenAnswer(invocation -> {
                Path path = invocation.getArgument(0);
                if (path.equals(absolutePath)) {
                    throw new IOException("Cannot create directory");
                }
                return Files.createDirectories(path); // Fallback to real Files.createDirectories for other paths
            });
            
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

