package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {
    private final String uploadDir = "uploads/documentos/";

    public String guardarArchivo(MultipartFile archivo, String subCarpeta) throws IOException {
        String nombreOriginal = archivo.getOriginalFilename();
        String nombreFinal = System.currentTimeMillis() + "_" + nombreOriginal;

        Path path = Paths.get(uploadDir + subCarpeta);
        if (!Files.exists(path)) Files.createDirectories(path);

        Files.copy(archivo.getInputStream(), path.resolve(nombreFinal), StandardCopyOption.REPLACE_EXISTING);
        return nombreFinal;
    }
}
