module com.example.recipecompendium {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.recipecompendium to javafx.fxml;
    opens com.example.recipecompendium.controllers to javafx.fxml;
    opens com.example.recipecompendium.models to javafx.fxml;
    opens com.example.recipecompendium.services to javafx.fxml;
    opens com.example.recipecompendium.utils to javafx.fxml;

    exports com.example.recipecompendium;
    exports com.example.recipecompendium.controllers;
    exports com.example.recipecompendium.models;
    exports com.example.recipecompendium.services;
    exports com.example.recipecompendium.utils;
}