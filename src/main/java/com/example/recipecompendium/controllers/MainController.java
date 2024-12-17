package com.example.recipecompendium.controllers;

import com.example.recipecompendium.models.Recipe;
import com.example.recipecompendium.models.User;
import com.example.recipecompendium.services.RecipeService;
import com.example.recipecompendium.utils.DialogUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

public class MainController {
    @FXML
    private Text welcomeText;
    @FXML
    private TextField searchField;
    @FXML
    private FlowPane recipeContainer;

    private User currentUser;
    private RecipeService recipeService;
    private List<Recipe> currentRecipes;

    public void initData(User user) {
        this.currentUser = user;
        this.recipeService = new RecipeService();
        welcomeText.setText("Welcome back, " + user.getUsername());
        loadAllRecipes();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void refreshRecipes() {
        loadAllRecipes();
    }

    private void loadAllRecipes() {
        try {
            currentRecipes = recipeService.getAllRecipes();
            displayRecipes(currentRecipes);
        } catch (Exception e) {
            DialogUtils.handleError.apply(e);
        }
    }

    private void displayRecipes(List<Recipe> recipes) {
        recipeContainer.getChildren().clear();
        recipes.forEach(this::createRecipeCard);
    }

    private void createRecipeCard(Recipe recipe) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/recipecompendium/recipe-card.fxml"));
            Parent card = loader.load();

            RecipeCardController controller = loader.getController();
            controller.setRecipe(recipe);
            controller.setMainController(this);

            recipeContainer.getChildren().add(card);
        } catch (IOException e) {
            DialogUtils.handleError.apply(e);
        }
    }

    @FXML
    protected void handleSearch() {
        String searchTerm = searchField.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            displayRecipes(currentRecipes);
        } else {
            Predicate<Recipe> searchFilter = recipe -> recipe.getName().toLowerCase().contains(searchTerm) ||
                    recipe.getDescription().toLowerCase().contains(searchTerm) ||
                    recipe.getCuisine().toLowerCase().contains(searchTerm);

            List<Recipe> filteredRecipes = currentRecipes.stream()
                    .filter(searchFilter)
                    .toList();

            displayRecipes(filteredRecipes);
        }
    }

    @FXML
    protected void handleHome() {
        loadAllRecipes();
    }

    @FXML
    protected void handleFavorites() {
        try {
            List<Recipe> favoriteRecipes = recipeService.getFavoriteRecipes(currentUser.getId());
            displayRecipes(favoriteRecipes);
        } catch (SQLException e) {
            DialogUtils.handleError.apply(e);
        }
    }

    @FXML
    protected void handleYourRecipes() {
        List<Recipe> userRecipes = currentRecipes.stream()
                .filter(recipe -> recipe.getCreatorId() == currentUser.getId())
                .toList();
        displayRecipes(userRecipes);
    }

    @FXML
    protected void handleCreateRecipe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/recipecompendium/recipe-edit-view.fxml"));
            Parent root = loader.load();
            
            RecipeEditController controller = loader.getController();
            controller.setMainController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Create New Recipe");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.handleError.apply(e);
        }
    }

    @FXML
    protected void handleLogout() {
        if (DialogUtils.confirmLogout.get()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/recipecompendium/login-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) welcomeText.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(false);
                stage.setWidth(800);
                stage.setHeight(600);
            } catch (IOException e) {
                DialogUtils.handleError.apply(e);
            }
        }
    }
}
