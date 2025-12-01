package com.gymflow.controller;

import com.gymflow.model.Role;
import com.gymflow.model.User;
import com.gymflow.security.SessionManager;
import com.gymflow.service.AuthService;
import com.gymflow.service.AuthServiceImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the login screen.
 * Handles user authentication and role-based navigation to dashboards.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private final AuthService authService;
    private final SessionManager sessionManager;

    public LoginController() {
        this.authService = new AuthServiceImpl();
        this.sessionManager = SessionManager.getInstance();
    }

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        
        // Allow login on Enter key press
        passwordField.setOnAction(event -> handleLogin());
        usernameField.setOnAction(event -> passwordField.requestFocus());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showErrorAlert("Login Error", "Please enter both username and password.");
            return;
        }

        try {
            // Authenticate user
            Optional<User> userOptional = authService.authenticate(username, password);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // Set session
                sessionManager.setCurrentUser(user);
                
                // Navigate to role-specific dashboard
                navigateToDashboard(user.getRole());
            } else {
                showErrorAlert("Login Failed", "Invalid username or password. Please try again.");
                passwordField.clear();
            }
        } catch (Exception e) {
            showErrorAlert("Login Error", "An error occurred during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(Role role) {
        try {
            String fxmlFile;
            switch (role) {
                case MEMBER:
                    fxmlFile = "/fxml/member-dashboard.fxml";
                    break;
                case TRAINER:
                    fxmlFile = "/fxml/trainer-dashboard.fxml";
                    break;
                case ADMIN:
                    fxmlFile = "/fxml/admin-dashboard.fxml";
                    break;
                default:
                    throw new IllegalArgumentException("Unknown role: " + role);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("GymFlow - " + role.name() + " Dashboard");
            stage.centerOnScreen();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
