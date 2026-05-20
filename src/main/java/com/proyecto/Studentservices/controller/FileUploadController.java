package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${upload.path:uploads/}")
    private String uploadPath;

    @PostMapping("/profile-picture")
    public ResponseEntity<ApiResponse<String>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {
        System.out.println("Content-Type recibido: " + file.getContentType());
        System.out.println("Nombre archivo: " + file.getOriginalFilename());
        try {
            // Validar que sea imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Solo se permiten imágenes", null));
            }

            // Validar tamaño máximo 5MB
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "La imagen no puede superar 5MB", null));
            }

            // Crear carpeta si no existe
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generar nombre único para el archivo
            String extension = getExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + extension;

            // Guardar archivo
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Retornar URL accesible
            String fileUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(new ApiResponse<>(true, "Imagen subida correctamente", fileUrl));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Error guardando imagen: " + e.getMessage(), null));
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}