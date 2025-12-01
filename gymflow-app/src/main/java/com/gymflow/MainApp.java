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
        System.out.println("=== GymFlow Application Starting ===");
        System.out.println("Initializing database...");
        try {
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.initialize();
            System.out.println("=== Database initialized successfully ===");
            System.out.println("Test users available:");
            System.out.println("  - member_demo / password123");
            System.out.println("  - trainer_demo / password123");
            System.out.println("  - admin_demo / password123");
        } catch (SQLException e) {
            System.err.println("=== ERROR: Failed to initialize database ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            // Continue anyway - might be using external database
            System.err.println("Continuing anyway - database may already be initialized");
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
