package com.example.recipecompendium.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class ImageService {
    private static final String IMAGE_DIRECTORY = "src/main/resources/images/";
    private static final String DEFAULT_IMAGE = "default-recipe.jpg";
    
    public String saveImage(File sourceFile) throws IOException {
        // Create images directory if it doesn't exist
        Path directoryPath = Paths.get(IMAGE_DIRECTORY);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
        
        // Generate unique filename to avoid conflicts
        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        Path targetPath = directoryPath.resolve(fileName);
        
        // Copy file to images directory
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return fileName;
    }
    
    public void deleteImage(String fileName) {
        if (fileName != null && !fileName.equals(DEFAULT_IMAGE)) {
            try {
                Path imagePath = Paths.get(IMAGE_DIRECTORY + fileName);
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.err.println("Error deleting image: " + e.getMessage());
            }
        }
    }
    
    public static String getDefaultImagePath() {
        return DEFAULT_IMAGE;
    }
}
