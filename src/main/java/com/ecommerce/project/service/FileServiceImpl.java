package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;


@Service
public class FileServiceImpl implements  FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // Ensure path is valid
        System.out.println("Path: " + path); // Debugging log

        // Get the original file name
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException("Original file name cannot be null");
        }

        // Generate a unique file name
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + fileName;

        // Create folder if it does not exist
        File folder = new File(path);
        if (!folder.exists()) {
            boolean created = folder.mkdirs(); // Ensures entire directory path is created
            if (created) {
                System.out.println("Folder created at: " + path);
            } else {
                throw new IOException("Failed to create folder at: " + path);
            }
        }

        // Copy file to the specified location
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }

}
