package com.example.recipecompendium.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class DialogUtils {
    public static final Supplier<Boolean> confirmLogout = () -> {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Any unsaved changes will be lost.");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    };
    
    public static final Supplier<Boolean> confirmDelete = () -> {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this recipe?");
        alert.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    };

    public static final BiConsumer<String, String> showError = (header, message) -> {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    };

    public static final Consumer<Exception> showErrorDialog = e -> {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    };

    public static final Function<Exception, Void> handleError = e -> {
        e.printStackTrace();
        showErrorDialog.accept(e);
        return null;
    };
}
