package com.gymflow;

import com.gymflow.config.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Entry point for the GymFlow JavaFX application.
 */
public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        // Initialize database before starting the UI
        try {
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.initialize();
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            // Continue anyway - might be using external database
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 960, 600);
        stage.setTitle("GymFlow");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
