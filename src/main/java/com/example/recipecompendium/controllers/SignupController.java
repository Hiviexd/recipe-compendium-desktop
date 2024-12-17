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

public class SignupController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private final UserService userService = new UserService();

    @FXML
    protected void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            DialogUtils.showError.accept("Signup Error", "Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            DialogUtils.showError.accept("Signup Error", "Passwords do not match");
            return;
        }

        try {
            if (userService.usernameExists(username)) {
                DialogUtils.showError.accept("Signup Error", "Username already exists");
                return;
            }

            User user = userService.createUser(username, password);
            if (user != null) {
                loadMainView(user);
            } else {
                DialogUtils.showError.accept("Signup Error", "Error creating account");
            }
        } catch (Exception e) {
            DialogUtils.handleError.apply(e);
        }
    }

    @FXML
    protected void handleLoginRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/recipecompendium/login-view.fxml"));
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
