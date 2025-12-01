package com.gymflow.controller;

import com.gymflow.model.ClassSession;
import com.gymflow.model.Member;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for the Member Dashboard.
 * Displays member-specific information and functionality.
 */
public class MemberDashboardController {

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
    private Button logoutButton;

    @FXML
    private TableView<WorkoutPlan> workoutTable;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutTitleColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutDescriptionColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutDifficultyColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutCreatedColumn;

    @FXML
    private TableView<ClassSession> classTable;

    @FXML
    private TableColumn<ClassSession, String> classNameColumn;

    @FXML
    private TableColumn<ClassSession, String> classTrainerColumn;

    @FXML
    private TableColumn<ClassSession, String> classDateTimeColumn;

    @FXML
    private TableColumn<ClassSession, Integer> classCapacityColumn;

    @FXML
    private TableColumn<ClassSession, String> classRegisteredColumn;

    private final SessionManager sessionManager;
    private final WorkoutService workoutService;
    private final ClassScheduleService classScheduleService;
    private final AttendanceService attendanceService;

    private ObservableList<WorkoutPlan> workoutPlans;
    private ObservableList<ClassSession> upcomingClasses;

    public MemberDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.workoutService = new WorkoutServiceImpl();
        this.classScheduleService = new ClassScheduleServiceImpl();
        this.attendanceService = new AttendanceServiceImpl();
    }

    @FXML
    private void initialize() {
        loadUserInfo();
        setupWorkoutTable();
        setupClassTable();
        loadWorkoutPlans();
        loadUpcomingClasses();
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
        }
    }

    private void setupWorkoutTable() {
        workoutTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        workoutDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
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

    private void setupClassTable() {
        classNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        classTrainerColumn.setCellValueFactory(cellData -> {
            // TODO: Get trainer name from database
            return new javafx.beans.property.SimpleStringProperty("Trainer");
        });
        classDateTimeColumn.setCellValueFactory(cellData -> {
            ClassSession session = cellData.getValue();
            if (session != null && session.getScheduleTimestamp() != null) {
                String formatted = session.getScheduleTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        classCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        classRegisteredColumn.setCellValueFactory(cellData -> {
            ClassSession session = cellData.getValue();
            User currentUser = sessionManager.getCurrentUser();
            if (session != null && currentUser != null && currentUser instanceof Member) {
                boolean isRegistered = attendanceService.isRegisteredForClass(session.getId(), currentUser.getId());
                return new javafx.beans.property.SimpleStringProperty(isRegistered ? "Yes" : "No");
            }
            return new javafx.beans.property.SimpleStringProperty("No");
        });
    }

    private void loadWorkoutPlans() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser instanceof Member) {
            workoutPlans = FXCollections.observableArrayList(
                workoutService.getWorkoutPlansForMember(currentUser.getId())
            );
            workoutTable.setItems(workoutPlans);
        } else {
            workoutPlans = FXCollections.observableArrayList();
            workoutTable.setItems(workoutPlans);
        }
    }

    private void loadUpcomingClasses() {
        upcomingClasses = FXCollections.observableArrayList(
            classScheduleService.getUpcomingClassSessions()
        );
        classTable.setItems(upcomingClasses);
    }

    @FXML
    private void handleRegisterForClass() {
        ClassSession selectedSession = classTable.getSelectionModel().getSelectedItem();
        if (selectedSession == null) {
            showErrorAlert("No Selection", "Please select a class to register for");
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Member)) {
            showErrorAlert("Error", "No member logged in");
            return;
        }

        // Check if already registered
        if (attendanceService.isRegisteredForClass(selectedSession.getId(), currentUser.getId())) {
            showErrorAlert("Already Registered", "You are already registered for this class");
            return;
        }

        // Check capacity
        int registered = attendanceService.getRegisteredCount(selectedSession.getId());
        if (registered >= selectedSession.getCapacity()) {
            showErrorAlert("Class Full", "This class has reached its capacity");
            return;
        }

        // Register
        Optional<com.gymflow.model.AttendanceRecord> result = attendanceService.registerForClass(
            selectedSession.getId(), currentUser.getId()
        );

        if (result.isPresent()) {
            showSuccessAlert("Success", "Successfully registered for '" + selectedSession.getTitle() + "'");
            loadUpcomingClasses(); // Refresh the table
        } else {
            showErrorAlert("Error", "Failed to register for class");
        }
    }

    @FXML
    private void handleUnregisterFromClass() {
        ClassSession selectedSession = classTable.getSelectionModel().getSelectedItem();
        if (selectedSession == null) {
            showErrorAlert("No Selection", "Please select a class to unregister from");
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Member)) {
            showErrorAlert("Error", "No member logged in");
            return;
        }

        // Check if registered
        if (!attendanceService.isRegisteredForClass(selectedSession.getId(), currentUser.getId())) {
            showErrorAlert("Not Registered", "You are not registered for this class");
            return;
        }

        // Confirm unregistration
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Unregistration");
        confirmDialog.setHeaderText("Unregister from Class");
        confirmDialog.setContentText("Are you sure you want to unregister from '" + selectedSession.getTitle() + "'?");

        Optional<javafx.scene.control.ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            boolean success = attendanceService.unregisterFromClass(
                selectedSession.getId(), currentUser.getId()
            );

            if (success) {
                showSuccessAlert("Success", "Successfully unregistered from '" + selectedSession.getTitle() + "'");
                loadUpcomingClasses(); // Refresh the table
            } else {
                showErrorAlert("Error", "Failed to unregister from class");
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
