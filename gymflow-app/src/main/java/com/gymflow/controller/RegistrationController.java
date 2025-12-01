package com.gymflow.controller;

import com.gymflow.model.Role;
import com.gymflow.model.User;
import com.gymflow.security.SessionManager;
import com.gymflow.service.AuthService;
import com.gymflow.service.AuthServiceImpl;
import com.gymflow.service.UserService;
import com.gymflow.service.UserServiceImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the registration/sign-up screen.
 * Handles new user account creation.
 */
public class RegistrationController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button registerButton;

    @FXML
    private Button backToLoginButton;

    private final UserService userService;
    private final AuthService authService;
    private final SessionManager sessionManager;

    public RegistrationController() {
        this.userService = new UserServiceImpl();
        this.authService = new AuthServiceImpl();
        this.sessionManager = SessionManager.getInstance();
    }

    @FXML
    private void initialize() {
        // Populate role combo box (only MEMBER and TRAINER for self-registration)
        roleComboBox.getItems().addAll("MEMBER", "TRAINER");
        roleComboBox.setValue("MEMBER"); // Default to MEMBER

        registerButton.setOnAction(event -> handleRegister());
        backToLoginButton.setOnAction(event -> handleBackToLogin());

        // Allow registration on Enter key press
        confirmPasswordField.setOnAction(event -> handleRegister());
    }

    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String selectedRole = roleComboBox.getValue();

        // Validate input
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorAlert("Registration Error", "Please fill in all fields.");
            return;
        }

        // Validate email format (basic)
        if (!email.contains("@") || !email.contains(".")) {
            showErrorAlert("Registration Error", "Please enter a valid email address.");
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            showErrorAlert("Registration Error", "Passwords do not match.");
            passwordField.clear();
            confirmPasswordField.clear();
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            showErrorAlert("Registration Error", "Password must be at least 6 characters long.");
            return;
        }

        // Validate role selection
        if (selectedRole == null) {
            showErrorAlert("Registration Error", "Please select a role.");
            return;
        }

        try {
            Role role = Role.fromString(selectedRole);
            
            // Create new user
            Optional<User> newUser = userService.createUser(username, password, fullName, email, role);

            if (newUser.isPresent()) {
                // Registration successful - automatically log in
                sessionManager.setCurrentUser(newUser.get());
                
                showSuccessAlert("Registration Successful", 
                    "Account created successfully! You are now logged in.");
                
                // Navigate to dashboard
                navigateToDashboard(role);
            } else {
                showErrorAlert("Registration Failed", 
                    "Failed to create account. Username may already be taken.");
            }
        } catch (Exception e) {
            showErrorAlert("Registration Error", 
                "An error occurred during registration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backToLoginButton.getScene().getWindow();
            Scene scene = new Scene(root, 960, 600);
            stage.setScene(scene);
            stage.setTitle("GymFlow - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load login screen: " + e.getMessage());
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
            
            Stage stage = (Stage) registerButton.getScene().getWindow();
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

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

