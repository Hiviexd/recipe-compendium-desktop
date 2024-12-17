package com.example.recipecompendium.controllers;

import com.example.recipecompendium.models.Recipe;
import com.example.recipecompendium.services.ImageService;
import com.example.recipecompendium.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class RecipeDetailController {
    @FXML
    private ImageView recipeImage;
    @FXML
    private Text titleText;
    @FXML
    private Text cuisineText;
    @FXML
    private Text descriptionText;
    @FXML
    private Text ingredientsText;
    @FXML
    private Text stepsText;
    @FXML
    private Text creatorText;

    // Function to load image with fallback to default
    private final Function<String, Image> loadImage = imagePath -> {
        try {
            String path = (imagePath != null && !imagePath.isEmpty())
                    ? imagePath
                    : ImageService.getDefaultImagePath();
            return new Image(getClass().getResourceAsStream("/images/" + path));
        } catch (Exception e) {
            return new Image(getClass().getResourceAsStream("/images/" + ImageService.getDefaultImagePath()));
        }
    };

    // Function to get username with error handling
    private final Function<Integer, String> getUsernameById = userId -> {
        try {
            return new UserService().getUserById(userId).getUsername();
        } catch (SQLException e) {
            return "Unknown User";
        }
    };

    // Consumer to update all UI elements
    private final Consumer<Recipe> updateUIElements = recipe -> {
        titleText.setText(recipe.getName());
        cuisineText.setText(recipe.getCuisine());
        descriptionText.setText(recipe.getDescription());
        ingredientsText.setText(recipe.getIngredients());
        stepsText.setText(recipe.getSteps());
        creatorText.setText("Created by: " + getUsernameById.apply(recipe.getCreatorId()));
        recipeImage.setImage(loadImage.apply(recipe.getImageUrl()));
    };

    public void setRecipe(Recipe recipe) {
        updateUIElements.accept(recipe);
    }

    @FXML
    protected void handleClose() {
        Stage stage = (Stage) titleText.getScene().getWindow();
        stage.close();
    }
}
