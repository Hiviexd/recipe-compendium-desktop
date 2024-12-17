package com.example.recipecompendium.controllers;

import com.example.recipecompendium.models.User;
import com.example.recipecompendium.services.UserService;
import com.example.recipecompendium.utils.DialogUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            DialogUtils.showError.accept("Login Error", "Please fill in all fields");
            return;
        }

        try {
            User user = userService.authenticateUser(username, password);
            if (user != null) {
                loadMainView(user);
            } else {
                DialogUtils.showError.accept("Login Error", "Invalid username or password");
            }
        } catch (Exception e) {
            DialogUtils.handleError.apply(e);
        }
    }

    @FXML
    protected void handleSignupRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/recipecompendium/signup-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            DialogUtils.handleError.apply(e);
        }
    }

    private void loadMainView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/recipecompendium/main-view.fxml"));
            Parent root = loader.load();
            
            MainController mainController = loader.getController();
            mainController.initData(user);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException e) {
            DialogUtils.handleError.apply(e);
        }
    }
}
