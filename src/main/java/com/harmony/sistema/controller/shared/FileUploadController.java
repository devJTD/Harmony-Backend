package com.harmony.sistema.controller.shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:4200")
public class FileUploadController {

    private static final String UPLOAD_DIR = "src/main/resources/static/images/";

    /**
     * Endpoint para listar todos los archivos disponibles en la carpeta de
     * imágenes.
     * GET /api/upload/images-list
     */
    @GetMapping("/images-list")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> listImages() {
        System.out.println("📱 [FILE UPLOAD] GET /api/upload/images-list - Listando imágenes disponibles");

        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> images = new ArrayList<>();

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                System.out.println("⚠️ [FILE UPLOAD] Directorio no existe: " + UPLOAD_DIR);
                response.put("success", true);
                response.put("images", images);
                return ResponseEntity.ok(response);
            }

            Files.list(uploadPath)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        String filename = filePath.getFileName().toString();
                        String imageUrl = "/images/" + filename;

                        Map<String, String> imageInfo = new HashMap<>();
                        imageInfo.put("filename", filename);
                        imageInfo.put("url", imageUrl);

                        images.add(imageInfo);
                    });

            System.out.println("✅ [FILE UPLOAD] " + images.size() + " imágenes encontradas");
            response.put("success", true);
            response.put("images", images);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.out.println("❌ [FILE UPLOAD] Error al listar imágenes: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al listar imágenes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para subir un archivo de imagen.
     * POST /api/upload/image
     */
    @PostMapping("/image")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        System.out.println("📱 [FILE UPLOAD] Iniciando subida de archivo: " + file.getOriginalFilename());

        Map<String, Object> response = new HashMap<>();

        try {
            // Validar que el archivo no esté vacío
            if (file.isEmpty()) {
                System.out.println("❌ [FILE UPLOAD] Archivo vacío");
                response.put("success", false);
                response.put("message", "El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar tipo de archivo (solo imágenes)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                System.out.println("❌ [FILE UPLOAD] Tipo de archivo no permitido: " + contentType);
                response.put("success", false);
                response.put("message", "Solo se permiten archivos de imagen");
                return ResponseEntity.badRequest().body(response);
            }

            // ✅ MANTENER el nombre original del archivo (SIN UUID)
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "imagen.jpg";
            }

            System.out.println("📝 [FILE UPLOAD] Nombre original: " + originalFilename);

            // Crear directorio si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("📁 [FILE UPLOAD] Directorio creado: " + UPLOAD_DIR);
            }

            // ✅ Guardar con el nombre original (REPLACE_EXISTING si ya existe)
            Path filePath = uploadPath.resolve(originalFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ [FILE UPLOAD] Archivo guardado en: " + filePath);

            // Construir URL relativa para la BD (SOLO LA RUTA STRING)
            String imageUrl = "/images/" + originalFilename;
            System.out.println("🔗 [FILE UPLOAD] URL de imagen: " + imageUrl);

            response.put("success", true);
            response.put("message", "Imagen subida exitosamente");
            response.put("imageUrl", imageUrl);
            response.put("filename", originalFilename);

            System.out.println("✅ [FILE UPLOAD SUCCESS] Imagen guardada correctamente\n");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.out.println("❌ [FILE UPLOAD] Error al guardar archivo: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error al subir la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}