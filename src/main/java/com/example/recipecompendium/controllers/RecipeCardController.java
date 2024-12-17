package com.example.recipecompendium.controllers;

import com.example.recipecompendium.models.Recipe;
import com.example.recipecompendium.services.RecipeService;
import com.example.recipecompendium.services.UserService;
import com.example.recipecompendium.services.ImageService;
import com.example.recipecompendium.utils.DialogUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RecipeCardController {
    @FXML private ImageView recipeImage;
    @FXML private Text nameText;
    @FXML private Text descriptionText;
    @FXML private Text cuisineText;
    @FXML private Text creatorText;
    @FXML private ToggleButton favoriteButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    
    private Recipe recipe;
    private MainController mainController;
    private final RecipeService recipeService = new RecipeService();
    private final UserService userService = new UserService();

    // Truncates text based on given value
    private final BiFunction<String, Integer, String> truncator = (text, maxLength) ->
        text != null ? (text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...") : "";

    private final Function<Integer, String> getUsernameById = userId -> {
        try {
            return userService.getUserById(userId).getUsername();
        } catch (SQLException e) {
            return "Unknown User";
        }
    };
        
    private final Consumer<Recipe> updateUIElements = recipe -> {
        nameText.setText(recipe.getName());
        descriptionText.setText(truncator.apply(recipe.getDescription(), 70));
        cuisineText.setText(recipe.getCuisine());
        creatorText.setText("Created by: " + getUsernameById.apply(recipe.getCreatorId()));
        loadRecipeImage(recipe);
        updateButtonVisibility();
    };
    
    private final Function<Recipe, Image> loadImage = recipe -> {
        String imagePath = recipe.getImageUrl();
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = ImageService.getDefaultImagePath();
        }
        try {
            return new Image(getClass().getResourceAsStream("/images/" + imagePath));
        } catch (Exception e) {
            return new Image(getClass().getResourceAsStream("/images/" + ImageService.getDefaultImagePath()));
        }
    };
    
    private final Predicate<Recipe> isCreatorOfRecipe = recipe ->
        mainController != null && recipe.getCreatorId() == mainController.getCurrentUser().getId();
    
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
        updateUIElements.accept(recipe);
        updateFavoriteStatus();
    }
    
    private void updateFavoriteStatus() {
        if (mainController != null && recipe != null) {
            try {
                boolean isFavorite = recipeService.isFavorite(
                    mainController.getCurrentUser().getId(),
                    recipe.getId()
                );
                favoriteButton.setSelected(isFavorite);
            } catch (SQLException e) {
                DialogUtils.handleError.apply(e);
            }
        }
    }
    
    private void loadRecipeImage(Recipe recipe) {
        recipeImage.setImage(loadImage.apply(recipe));
    }
    
    private void updateButtonVisibility() {
        boolean isCreator = recipe != null && isCreatorOfRecipe.test(recipe);
        editButton.setVisible(isCreator);
        deleteButton.setVisible(isCreator);
    }
    
    @FXML
    protected void handleCardClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/recipecompendium/recipe-detail-view.fxml"));
            Parent root = loader.load();
            
            RecipeDetailController controller = loader.getController();
            controller.setRecipe(recipe);
            
            Stage stage = new Stage();
            stage.setTitle(recipe.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.handleError.apply(e);
        }
    }
    
    @FXML
    protected void handleFavoriteToggle() {
        try {
            recipeService.toggleFavorite(
                mainController.getCurrentUser().getId(),
                recipe.getId(),
                favoriteButton.isSelected()
            );
        } catch (Exception e) {
            DialogUtils.handleError.apply(e);
            favoriteButton.setSelected(!favoriteButton.isSelected());
        }
    }
    
    @FXML
    protected void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/recipecompendium/recipe-edit-view.fxml"));
            Parent root = loader.load();
            
            RecipeEditController controller = loader.getController();
            controller.setRecipe(recipe);
            controller.setMainController(mainController);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Recipe");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh the detail view with updated recipe data
            Recipe updatedRecipe = recipeService.getRecipeById(recipe.getId());
            if (updatedRecipe != null) {
                setRecipe(updatedRecipe);
            }
        } catch (Exception e) {
            DialogUtils.handleError.apply(e);
        }
    }

    @FXML
    protected void handleDelete() {
        if (DialogUtils.confirmDelete.get()) {
            try {
                recipeService.deleteRecipe(recipe.getId());
                mainController.refreshRecipes();
            } catch (SQLException e) {
                DialogUtils.handleError.apply(e);
            }
        }
    }

    @FXML
    public void initialize() {
        setupFavoriteButton();
    }
    
    private void setupFavoriteButton() {
        FontIcon heartIcon = new FontIcon("far-heart");
        heartIcon.getStyleClass().add("favorite-icon");
        favoriteButton.setGraphic(heartIcon);
        
        favoriteButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> 
            heartIcon.setIconLiteral(isSelected ? "fas-heart" : "far-heart"));
    }
    
    public void setMainController(MainController controller) {
        this.mainController = controller;
        updateButtonVisibility();
        // Check favorite status after mainController is set
        if (recipe != null) {
            try {
                boolean isFavorite = recipeService.isFavorite(
                    controller.getCurrentUser().getId(),
                    recipe.getId()
                );
                favoriteButton.setSelected(isFavorite);
            } catch (SQLException e) {
                DialogUtils.handleError.apply(e);
            }
        }
    }
}
