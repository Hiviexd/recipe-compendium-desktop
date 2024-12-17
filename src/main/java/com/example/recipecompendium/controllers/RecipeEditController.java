package com.example.recipecompendium.controllers;

import com.example.recipecompendium.models.Recipe;
import com.example.recipecompendium.services.RecipeService;
import com.example.recipecompendium.services.ImageService;
import com.example.recipecompendium.utils.DialogUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Consumer;

public class RecipeEditController {
    @FXML private Text titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField cuisineField;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea ingredientsArea;
    @FXML private TextArea stepsArea;
    @FXML private TextField imageUrlField;
    @FXML private Label messageLabel;
    
    private Recipe recipe;
    private MainController mainController;
    private final RecipeService recipeService = new RecipeService();
    private final ImageService imageService = new ImageService();
    private boolean isNewRecipe;

    private final Predicate<String> isNotEmpty = str -> str != null && !str.trim().isEmpty();

    // Field mapping function
    private final Function<Recipe, Map<String, String>> extractFields = recipe -> {
        Map<String, String> fields = new HashMap<>();
        fields.put("name", recipe.getName());
        fields.put("cuisine", recipe.getCuisine());
        fields.put("description", recipe.getDescription());
        fields.put("ingredients", recipe.getIngredients());
        fields.put("steps", recipe.getSteps());
        return fields;
    };
    
    // Field population function
    private final Consumer<Recipe> populateFields = recipe -> {
        nameField.setText(recipe.getName());
        cuisineField.setText(recipe.getCuisine());
        descriptionArea.setText(recipe.getDescription());
        ingredientsArea.setText(recipe.getIngredients());
        stepsArea.setText(recipe.getSteps());
        imageUrlField.setText(recipe.getImageUrl());
    };
    
    public void initialize() {
        isNewRecipe = true;
        titleLabel.setText("Create New Recipe");
    }
    
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
        isNewRecipe = false;
        titleLabel.setText("Edit Recipe");
        populateFields.accept(recipe);
    }
    
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }
    
    @FXML
    protected void handleImageBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Recipe Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedImageFile = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        if (selectedImageFile != null) {
            try {
                String fileName = imageService.saveImage(selectedImageFile);
                imageUrlField.setText(fileName);
            } catch (IOException e) {
                DialogUtils.showError.accept("Image Error", "Failed to save image: " + e.getMessage());
            }
        }
    }
    
    @FXML
    protected void handleSave() {
        if (!validateInput()) return;

        try {
            Recipe updatedRecipe = isNewRecipe ? createNewRecipe() : updateExistingRecipe();
            if (updatedRecipe != null) {
                mainController.refreshRecipes();
                closeWindow();
            }
        } catch (Exception e) {
            DialogUtils.showError.accept("Save Error", "Error saving recipe: " + e.getMessage());
        }
    }
    
    private boolean validateInput() {
        Recipe tempRecipe = new Recipe(
            0,
            nameField.getText().trim(),
            descriptionArea.getText().trim(),
            imageUrlField.getText().trim(),
            ingredientsArea.getText().trim(),
            stepsArea.getText().trim(),
            cuisineField.getText().trim(),
            mainController.getCurrentUser().getId()
        );
        
        Map<String, String> fields = extractFields.apply(tempRecipe);

        return fields.entrySet().stream()
            .filter(entry -> !isNotEmpty.test(entry.getValue()))
            .findFirst()
            .map(entry -> {
                messageLabel.setText(entry.getKey() + " is required");
                return false;
            })
            .orElse(true);
    }
    
    private Recipe createNewRecipe() throws Exception {
        String imageFileName = imageUrlField.getText().trim();
        if (imageFileName.isEmpty()) {
            imageFileName = ImageService.getDefaultImagePath();
        }
        
        Recipe newRecipe = new Recipe(
            0,
            nameField.getText().trim(),
            descriptionArea.getText().trim(),
            imageFileName,
            ingredientsArea.getText().trim(),
            stepsArea.getText().trim(),
            cuisineField.getText().trim(),
            mainController.getCurrentUser().getId()
        );
        
        return recipeService.createRecipe(newRecipe);
    }
    
    private Recipe updateExistingRecipe() throws Exception {
        String oldImageUrl = recipe.getImageUrl();
        String newImageUrl = imageUrlField.getText().trim();
        
        // Delete old image if it's being replaced and isn't the default
        if (!oldImageUrl.equals(newImageUrl)) {
            imageService.deleteImage(oldImageUrl);
        }
        
        recipe.setName(nameField.getText().trim());
        recipe.setDescription(descriptionArea.getText().trim());
        recipe.setImageUrl(newImageUrl);
        recipe.setIngredients(ingredientsArea.getText().trim());
        recipe.setSteps(stepsArea.getText().trim());
        recipe.setCuisine(cuisineField.getText().trim());
        
        recipeService.updateRecipe(recipe);
        return recipe;
    }
    
    @FXML
    protected void handleCancel() {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
