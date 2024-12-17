package com.example.recipecompendium.services;

import com.example.recipecompendium.models.Recipe;
import com.example.recipecompendium.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class RecipeService {
    
    private final Map<Integer, Recipe> recipeCache = new HashMap<>();
    private final Map<String, Boolean> favoriteCache = new HashMap<>();
    
    private final Function<ResultSet, Recipe> resultSetToRecipe = rs -> {
        try {
            Recipe recipe = new Recipe(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("image_url"),
                rs.getString("ingredients"),
                rs.getString("steps"),
                rs.getString("cuisine"),
                rs.getInt("creator_id")
            );
            recipeCache.put(recipe.getId(), recipe);
            return recipe;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    private String getFavoriteKey(int userId, int recipeId) {
        return userId + "-" + recipeId;
    }
    
    public boolean isFavorite(int userId, int recipeId) throws SQLException {
        String key = getFavoriteKey(userId, recipeId);
        if (favoriteCache.containsKey(key)) {
            return favoriteCache.get(key);
        }
        
        String query = "SELECT 1 FROM favorites WHERE user_id = ? AND recipe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, recipeId);
            ResultSet rs = stmt.executeQuery();
            
            boolean isFavorite = rs.next();
            favoriteCache.put(key, isFavorite);
            return isFavorite;
        }
    }
    
    public void toggleFavorite(int userId, int recipeId, boolean favorite) throws SQLException {
        String key = getFavoriteKey(userId, recipeId);
        if (favorite) {
            String insert = "INSERT INTO favorites (user_id, recipe_id) VALUES (?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insert)) {
                
                stmt.setInt(1, userId);
                stmt.setInt(2, recipeId);
                stmt.executeUpdate();
                favoriteCache.put(key, true);
            }
        } else {
            String delete = "DELETE FROM favorites WHERE user_id = ? AND recipe_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(delete)) {
                
                stmt.setInt(1, userId);
                stmt.setInt(2, recipeId);
                stmt.executeUpdate();
                favoriteCache.put(key, false);
            }
        }
    }
    
    public List<Recipe> getFavoriteRecipes(int userId) throws SQLException {
        String query = "SELECT r.* FROM recipes r " +
                      "JOIN favorites f ON r.id = f.recipe_id " +
                      "WHERE f.user_id = ?";
        
        List<Recipe> favorites = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                favorites.add(resultSetToRecipe.apply(rs));
            }
        }
        return favorites;
    }

    public List<Recipe> getAllRecipes() throws SQLException {
        String query = "SELECT * FROM recipes";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            List<Recipe> recipes = new ArrayList<>();
            while (rs.next()) {
                recipes.add(resultSetToRecipe.apply(rs));
            }
            return recipes;
        }
    }
    
    public Recipe getRecipeById(int id) throws SQLException {
        // Check cache first
        if (recipeCache.containsKey(id)) {
            return recipeCache.get(id);
        }
        
        String query = "SELECT * FROM recipes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return resultSetToRecipe.apply(rs);
            }
        }
        return null;
    }
    
    public Recipe createRecipe(Recipe recipe) throws SQLException {
        String query = "INSERT INTO recipes (name, description, image_url, ingredients, " +
                      "steps, cuisine, creator_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            setRecipeParameters(stmt, recipe);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating recipe failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    recipe.setId(generatedKeys.getInt(1));
                    return recipe;
                } else {
                    throw new SQLException("Creating recipe failed, no ID obtained.");
                }
            }
        }
    }
    
    public void updateRecipe(Recipe recipe) throws SQLException {
        String query = "UPDATE recipes SET name = ?, description = ?, image_url = ?, " +
                      "ingredients = ?, steps = ?, cuisine = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, recipe.getName());
            stmt.setString(2, recipe.getDescription());
            stmt.setString(3, recipe.getImageUrl());
            stmt.setString(4, recipe.getIngredients());
            stmt.setString(5, recipe.getSteps());
            stmt.setString(6, recipe.getCuisine());
            stmt.setInt(7, recipe.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                recipeCache.put(recipe.getId(), recipe);
            } else {
                throw new SQLException("Updating recipe failed, no rows affected.");
            }
        }
    }
    
    public void deleteRecipe(int id) throws SQLException {
        String query = "DELETE FROM recipes WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Remove from cache if successful
                recipeCache.remove(id);
            }
        }
    }
    
    private void setRecipeParameters(PreparedStatement stmt, Recipe recipe) throws SQLException {
        stmt.setString(1, recipe.getName());
        stmt.setString(2, recipe.getDescription());
        stmt.setString(3, recipe.getImageUrl());
        stmt.setString(4, recipe.getIngredients());
        stmt.setString(5, recipe.getSteps());
        stmt.setString(6, recipe.getCuisine());
        stmt.setInt(7, recipe.getCreatorId());
    }
}
