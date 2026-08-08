package com.teslo.shop.files.controller;

import com.teslo.shop.common.exception.ApiBadRequestException;
import com.teslo.shop.config.AppProperties;
import com.teslo.shop.files.service.FilesService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FilesController {

    private static final Set<String> VALID_EXTENSIONS = Set.of(
        "jpg",
        "jpeg",
        "png",
        "webp"
    );

    private static final Set<String> VALID_EXTENSIONS_PDF = Set.of("pdf");

    @Autowired
    private FilesService filesService;

    @Autowired
    private AppProperties properties;

    @GetMapping("/product/{imageName}")
    public ResponseEntity<Resource> findProductImage(
        @PathVariable String imageName
    ) {
        File file = filesService.getStaticProductImage(imageName);
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
            .contentType(resolveMediaType(file))
            .body(resource);
    }

    @GetMapping("/pdf/{pdfName}")
    public ResponseEntity<Resource> findProductPdf(
        @PathVariable String pdfName
    ) {
        File file = filesService.getStaticProductPdf(pdfName);
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
            .contentType(resolveMediaType(file))
            .body(resource);
    }

    @PostMapping("/product")
    public Map<String, String> uploadProductImage(
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiBadRequestException(
                "Make sure that the file is an image"
            );
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(
                originalName.lastIndexOf(".") + 1
            );
        }

        if (!VALID_EXTENSIONS.contains(extension)) {
            throw new ApiBadRequestException(
                "Make sure that the file is an image"
            );
        }

        String fileName = UUID.randomUUID() + "." + extension;

        try {
            Path uploadDir = Paths.get(
                filesService.getUploadDir().getAbsolutePath()
            )
                .toAbsolutePath()
                .normalize();
            Path targetPath = uploadDir.resolve(fileName);
            Files.copy(
                file.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + fileName, e);
        }

        String secureUrl =
            properties.getHostApi() + "/files/product/" + fileName;
        return Map.of("secureUrl", secureUrl, "fileName", fileName);
    }

    @PostMapping("/pdf")
    public Map<String, String> uploadProductPdf(
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiBadRequestException(
                "Make sure that the file is a PDF"
            );
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(
                originalName.lastIndexOf(".") + 1
            );
        }

        if (!isValidExtension(extension)) {
            throw new ApiBadRequestException(
                "Make sure that the file is a PDF"
            );
        }

        String fileName = UUID.randomUUID() + "." + extension;

        try {
            Path uploadDir = Paths.get(
                filesService.getUploadDirPdf().getAbsolutePath()
            )
                .toAbsolutePath()
                .normalize();
            Path targetPath = uploadDir.resolve(fileName);
            Files.copy(
                file.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + fileName, e);
        }

        String secureUrl = properties.getHostApi() + "/files/pdf/" + fileName;
        return Map.of("secureUrl", secureUrl, "fileName", fileName);
    }

    private MediaType resolveMediaType(File file) {
        String name = file.getName();
        String extension = name
            .substring(name.lastIndexOf('.') + 1)
            .toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "pdf" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private boolean isValidExtension(String extension) {
        return VALID_EXTENSIONS_PDF.contains(extension);
    }
}
