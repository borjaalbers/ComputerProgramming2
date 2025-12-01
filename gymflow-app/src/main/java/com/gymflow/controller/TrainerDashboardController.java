package com.gymflow.controller;

import com.gymflow.model.ClassSession;
import com.gymflow.model.Trainer;
import com.gymflow.model.User;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.security.SessionManager;
import com.gymflow.service.AttendanceService;
import com.gymflow.service.AttendanceServiceImpl;
import com.gymflow.service.ClassScheduleService;
import com.gymflow.service.ClassScheduleServiceImpl;
import com.gymflow.service.WorkoutService;
import com.gymflow.service.WorkoutServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for the Trainer Dashboard.
 * Displays trainer-specific information and functionality.
 */
public class TrainerDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label specializationLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<ClassSession> classTable;

    @FXML
    private TableColumn<ClassSession, String> classNameColumn;

    @FXML
    private TableColumn<ClassSession, String> classDateTimeColumn;

    @FXML
    private TableColumn<ClassSession, Integer> classCapacityColumn;

    @FXML
    private TableView<WorkoutPlan> workoutTable;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutTitleColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutMemberColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutDifficultyColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutCreatedColumn;

    private final SessionManager sessionManager;
    private final WorkoutService workoutService;
    private final ClassScheduleService classScheduleService;
    private final AttendanceService attendanceService;

    private ObservableList<ClassSession> classSessions;
    private ObservableList<WorkoutPlan> workoutPlans;

    public TrainerDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.workoutService = new WorkoutServiceImpl();
        this.classScheduleService = new ClassScheduleServiceImpl();
        this.attendanceService = new AttendanceServiceImpl();
    }

    @FXML
    private void initialize() {
        loadUserInfo();
        setupClassTable();
        setupWorkoutTable();
        loadClassSessions();
        loadWorkoutPlans();
        logoutButton.setOnAction(event -> handleLogout());
    }

    private void loadUserInfo() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
            usernameLabel.setText(currentUser.getUsername());
            fullNameLabel.setText(currentUser.getFullName());
            emailLabel.setText(currentUser.getEmail());
            roleLabel.setText(currentUser.getRole().name());
            
            // Display specialization if user is a Trainer
            if (currentUser instanceof Trainer) {
                Trainer trainer = (Trainer) currentUser;
                String specialization = trainer.getSpecialization();
                if (specialization != null && !specialization.isBlank()) {
                    specializationLabel.setText(specialization);
                } else {
                    specializationLabel.setText("Not specified");
                }
            } else {
                specializationLabel.setText("N/A");
            }
        }
    }

    private void setupClassTable() {
        classNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        classDateTimeColumn.setCellValueFactory(cellData -> {
            ClassSession session = cellData.getValue();
            if (session != null && session.getScheduleTimestamp() != null) {
                String formatted = session.getScheduleTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        classCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
    }

    private void setupWorkoutTable() {
        workoutTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        workoutMemberColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            if (plan != null) {
                return new javafx.beans.property.SimpleStringProperty("Member #" + plan.getMemberId());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        workoutDifficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        workoutCreatedColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            if (plan != null && plan.getCreatedAt() != null) {
                String formatted = plan.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
    }

    private void loadClassSessions() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            classSessions = FXCollections.observableArrayList(
                classScheduleService.getClassSessionsByTrainer(currentUser.getId())
            );
            classTable.setItems(classSessions);
        } else {
            classSessions = FXCollections.observableArrayList();
            classTable.setItems(classSessions);
        }
    }

    private void loadWorkoutPlans() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            workoutPlans = FXCollections.observableArrayList(
                workoutService.getWorkoutPlansByTrainer(currentUser.getId())
            );
            workoutTable.setItems(workoutPlans);
        } else {
            workoutPlans = FXCollections.observableArrayList();
            workoutTable.setItems(workoutPlans);
        }
    }

    @FXML
    private void handleCreateClass() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            showErrorAlert("Error", "No user logged in");
            return;
        }

        // Simple dialog for creating a class (can be enhanced with a proper form later)
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Class");
        dialog.setHeaderText("Enter class details");
        dialog.setContentText("Class Title:");

        Optional<String> titleResult = dialog.showAndWait();
        if (titleResult.isPresent() && !titleResult.get().trim().isEmpty()) {
            String title = titleResult.get().trim();
            LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1); // Default to tomorrow
            int capacity = 20; // Default capacity

            Optional<ClassSession> created = classScheduleService.createClassSession(
                currentUser.getId(), title, scheduleTime, capacity
            );

            if (created.isPresent()) {
                showSuccessAlert("Success", "Class session created successfully!");
                loadClassSessions(); // Refresh the table
            } else {
                showErrorAlert("Error", "Failed to create class session");
            }
        }
    }

    @FXML
    private void handleCreateWorkoutPlan() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            showErrorAlert("Error", "No user logged in");
            return;
        }

        // Simple dialog for creating a workout plan (can be enhanced with a proper form later)
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Create Workout Plan");
        titleDialog.setHeaderText("Enter workout plan details");
        titleDialog.setContentText("Workout Plan Title:");

        Optional<String> titleResult = titleDialog.showAndWait();
        if (titleResult.isPresent() && !titleResult.get().trim().isEmpty()) {
            String title = titleResult.get().trim();
            
            // For now, we'll need a member ID - in a real app, you'd select from a list
            // For demo purposes, we'll use a placeholder
            TextInputDialog memberDialog = new TextInputDialog("1");
            memberDialog.setTitle("Create Workout Plan");
            memberDialog.setHeaderText("Enter member ID");
            memberDialog.setContentText("Member ID:");

            Optional<String> memberIdResult = memberDialog.showAndWait();
            if (memberIdResult.isPresent()) {
                try {
                    long memberId = Long.parseLong(memberIdResult.get().trim());
                    String description = "Custom workout plan";
                    String difficulty = "Intermediate";

                    Optional<WorkoutPlan> created = workoutService.createWorkoutPlan(
                        memberId, currentUser.getId(), title, description, difficulty
                    );

                    if (created.isPresent()) {
                        showSuccessAlert("Success", "Workout plan created successfully!");
                        loadWorkoutPlans(); // Refresh the table
                    } else {
                        showErrorAlert("Error", "Failed to create workout plan");
                    }
                } catch (NumberFormatException e) {
                    showErrorAlert("Error", "Invalid member ID");
                }
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            sessionManager.logout();
            navigateToLogin();
        } catch (Exception e) {
            showErrorAlert("Logout Error", "An error occurred during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, 960, 600);
            stage.setScene(scene);
            stage.setTitle("GymFlow - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load login screen: " + e.getMessage());
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
