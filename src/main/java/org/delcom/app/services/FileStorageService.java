package org.delcom.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path uploadPath;

    public FileStorageService(@Value("${app.upload.dir:./uploads/images}") String uploadDir) {
        try {
            // Gunakan path absolut untuk memastikan file persisten
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            this.uploadPath = basePath;
            
            // Buat direktori jika belum ada
            if (!Files.exists(this.uploadPath)) {
                Files.createDirectories(this.uploadPath);
            }
            
            System.out.println("Upload directory: " + this.uploadPath.toString());
        } catch (IOException e) {
            throw new RuntimeException("Tidak dapat membuat direktori upload: " + uploadDir, e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File kosong");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path targetLocation = this.uploadPath.resolve(filename);
            
            // Simpan file ke local storage
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Kembalikan path relatif untuk disimpan di database
            // Path ini akan diakses via /uploads/images/{filename}
            return "/uploads/images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename dari URL (format: /uploads/images/{filename})
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = this.uploadPath.resolve(filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log error but don't throw
            System.err.println("Gagal menghapus file: " + fileUrl + " - " + e.getMessage());
        }
    }
}

