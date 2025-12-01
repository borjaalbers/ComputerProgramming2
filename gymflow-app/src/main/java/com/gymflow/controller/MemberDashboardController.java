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
import java.util.List;
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
    private TableColumn<WorkoutPlan, String> workoutTypeColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutMuscleGroupColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutDifficultyColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutDurationColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutSetsRepsColumn;

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

    @FXML
    private TableColumn<ClassSession, String> classWorkoutPlanColumn;

    @FXML
    private javafx.scene.control.Button registerButton;

    @FXML
    private javafx.scene.control.Button unregisterButton;

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
        try {
            // Initialize collections first
            upcomingClasses = FXCollections.observableArrayList();
            workoutPlans = FXCollections.observableArrayList();
            
            loadUserInfo();
            setupWorkoutTable();
            setupClassTable();
            loadUpcomingClasses(); // Load classes first
            loadWorkoutPlans(); // Then load workout plans (which uses upcomingClasses)
            if (logoutButton != null) {
                logoutButton.setOnAction(event -> handleLogout());
            }
        } catch (Exception e) {
            System.err.println("Error initializing MemberDashboardController: " + e.getMessage());
            e.printStackTrace();
            // Show error to user
            javafx.application.Platform.runLater(() -> {
                showErrorAlert("Initialization Error", "Failed to initialize dashboard: " + e.getMessage());
            });
        }
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
        if (workoutTitleColumn == null || workoutTypeColumn == null || 
            workoutMuscleGroupColumn == null || workoutDifficultyColumn == null ||
            workoutDurationColumn == null || workoutSetsRepsColumn == null ||
            workoutCreatedColumn == null) {
            System.err.println("Warning: Some workout table columns are null");
            return;
        }
        
        workoutTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        workoutTypeColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            if (plan != null && plan.getWorkoutType() != null) {
                return new javafx.beans.property.SimpleStringProperty(plan.getWorkoutType());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        workoutMuscleGroupColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            if (plan != null && plan.getMuscleGroup() != null) {
                return new javafx.beans.property.SimpleStringProperty(plan.getMuscleGroup());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        workoutDifficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        
        workoutDurationColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            if (plan != null && plan.getDurationMinutes() != null) {
                return new javafx.beans.property.SimpleStringProperty(plan.getDurationMinutes() + " min");
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        workoutSetsRepsColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            if (plan != null) {
                Integer sets = plan.getTargetSets();
                Integer reps = plan.getTargetReps();
                if (sets != null && reps != null) {
                    return new javafx.beans.property.SimpleStringProperty(sets + " x " + reps);
                } else if (sets != null) {
                    return new javafx.beans.property.SimpleStringProperty(sets + " sets");
                } else if (reps != null) {
                    return new javafx.beans.property.SimpleStringProperty(reps + " reps");
                }
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
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
        if (classTable == null || classNameColumn == null || classTrainerColumn == null ||
            classDateTimeColumn == null || classCapacityColumn == null ||
            classRegisteredColumn == null || classWorkoutPlanColumn == null) {
            System.err.println("Warning: Some class table columns are null");
            return;
        }
        
        // Enable row selection
        classTable.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);
        
        classNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Get trainer name from database
        classTrainerColumn.setCellValueFactory(cellData -> {
            ClassSession session = cellData.getValue();
            if (session != null) {
                String trainerName = getTrainerName(session.getTrainerId());
                return new javafx.beans.property.SimpleStringProperty(trainerName);
            }
            return new javafx.beans.property.SimpleStringProperty("Unknown");
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
        
        // Registered column - use custom cell factory that always checks current state
        classRegisteredColumn.setCellFactory(column -> new javafx.scene.control.TableCell<ClassSession, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ClassSession session = getTableRow().getItem();
                    User currentUser = sessionManager.getCurrentUser();
                    if (session != null && currentUser != null && currentUser instanceof Member) {
                        // Always check current registration status from database
                        boolean isRegistered = attendanceService.isRegisteredForClass(
                            session.getId(), currentUser.getId()
                        );
                        setText(isRegistered ? "Yes" : "No");
                        // Visual feedback - green for registered, gray for not registered
                        if (isRegistered) {
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #7f8c8d;");
                        }
                    } else {
                        setText("No");
                        setStyle("-fx-text-fill: #7f8c8d;");
                    }
                }
            }
        });
        
        // Set a dummy cell value factory (required by JavaFX, but we use custom cell factory above)
        classRegisteredColumn.setCellValueFactory(cellData -> {
            ClassSession session = cellData.getValue();
            if (session != null) {
                // Return a property that will trigger cell updates
                return new javafx.beans.property.SimpleStringProperty("");
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Workout Plan column - shows the workout plan for the class
        classWorkoutPlanColumn.setCellValueFactory(cellData -> {
            ClassSession session = cellData.getValue();
            if (session != null && session.getWorkoutPlanId() != null) {
                // Get workout plan title
                String workoutTitle = getWorkoutPlanTitle(session.getWorkoutPlanId());
                return new javafx.beans.property.SimpleStringProperty(workoutTitle);
            }
            return new javafx.beans.property.SimpleStringProperty("No workout plan");
        });
        
        // Add visual feedback for selected row and update button states
        classTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButtonStates(newSelection);
        });
        
        // Add double-click to register and visual feedback for selection
        classTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<ClassSession> row = new javafx.scene.control.TableRow<ClassSession>() {
                @Override
                protected void updateItem(ClassSession item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        // Highlight selected row
                        if (isSelected()) {
                            setStyle("-fx-background-color: #e3f2fd;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };
            
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    row.setStyle("-fx-background-color: #e3f2fd;");
                } else {
                    row.setStyle("");
                }
            });
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    // Double-click to register
                    handleRegisterForClass();
                }
            });
            return row;
        });
        
        // Initialize button states
        updateButtonStates(null);
    }
    
    /**
     * Updates the enabled state of register/unregister buttons based on selection.
     */
    private void updateButtonStates(ClassSession selectedSession) {
        if (selectedSession == null) {
            if (registerButton != null) registerButton.setDisable(true);
            if (unregisterButton != null) unregisterButton.setDisable(true);
            return;
        }
        
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Member)) {
            if (registerButton != null) registerButton.setDisable(true);
            if (unregisterButton != null) unregisterButton.setDisable(true);
            return;
        }
        
        boolean isRegistered = attendanceService.isRegisteredForClass(
            selectedSession.getId(), currentUser.getId()
        );
        
        if (registerButton != null) {
            registerButton.setDisable(isRegistered);
        }
        if (unregisterButton != null) {
            unregisterButton.setDisable(!isRegistered);
        }
    }
    
    /**
     * Gets the trainer's name from the database.
     */
    private String getTrainerName(long trainerId) {
        try {
            com.gymflow.dao.UserDao userDao = new com.gymflow.dao.UserDaoImpl();
            Optional<User> trainer = userDao.findById(trainerId);
            if (trainer.isPresent()) {
                return trainer.get().getFullName();
            }
        } catch (Exception e) {
            System.err.println("Error getting trainer name: " + e.getMessage());
        }
        return "Trainer";
    }
    
    /**
     * Gets the workout plan title from the database.
     */
    private String getWorkoutPlanTitle(long workoutPlanId) {
        try {
            Optional<WorkoutPlan> plan = workoutService.getWorkoutPlanById(workoutPlanId);
            if (plan.isPresent()) {
                return plan.get().getTitle();
            }
        } catch (Exception e) {
            System.err.println("Error getting workout plan title: " + e.getMessage());
        }
        return "Unknown";
    }

    private void loadWorkoutPlans() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser instanceof Member) {
            // Get direct workout plans assigned to member
            List<WorkoutPlan> directPlans = workoutService.getWorkoutPlansForMember(currentUser.getId());
            
            // Get workout plans from registered classes
            List<WorkoutPlan> classPlans = new java.util.ArrayList<>();
            if (upcomingClasses != null) {
                for (ClassSession session : upcomingClasses) {
                    if (session != null && session.getWorkoutPlanId() != null && 
                        attendanceService.isRegisteredForClass(session.getId(), currentUser.getId())) {
                        Optional<WorkoutPlan> plan = workoutService.getWorkoutPlanById(session.getWorkoutPlanId());
                        plan.ifPresent(classPlans::add);
                    }
                }
            }
            
            // Combine both lists (avoid duplicates)
            java.util.Set<Long> planIds = new java.util.HashSet<>();
            List<WorkoutPlan> allPlans = new java.util.ArrayList<>();
            
            for (WorkoutPlan plan : directPlans) {
                if (!planIds.contains(plan.getId())) {
                    allPlans.add(plan);
                    planIds.add(plan.getId());
                }
            }
            
            for (WorkoutPlan plan : classPlans) {
                if (!planIds.contains(plan.getId())) {
                    allPlans.add(plan);
                    planIds.add(plan.getId());
                }
            }
            
            workoutPlans = FXCollections.observableArrayList(allPlans);
            workoutTable.setItems(workoutPlans);
        } else {
            workoutPlans = FXCollections.observableArrayList();
            workoutTable.setItems(workoutPlans);
        }
    }

    private void loadUpcomingClasses() {
        if (upcomingClasses == null) {
            upcomingClasses = FXCollections.observableArrayList();
        }
        
        List<ClassSession> sessions = classScheduleService.getUpcomingClassSessions();
        upcomingClasses.clear();
        upcomingClasses.addAll(sessions);
        
        if (classTable != null) {
            classTable.setItems(upcomingClasses);
            // Refresh the table to update cell values (especially Registered column)
            classTable.refresh();
        }
    }

    @FXML
    private void handleRegisterForClass() {
        ClassSession selectedSession = classTable.getSelectionModel().getSelectedItem();
        if (selectedSession == null) {
            showErrorAlert("No Selection", "Please select a class from the table to register for.");
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
            showErrorAlert("Class Full", "This class has reached its capacity (" + selectedSession.getCapacity() + " members)");
            return;
        }

        // Register
        Optional<com.gymflow.model.AttendanceRecord> result = attendanceService.registerForClass(
            selectedSession.getId(), currentUser.getId()
        );

        if (result.isPresent()) {
            showSuccessAlert("Success", "Successfully registered for '" + selectedSession.getTitle() + "'");
            // Refresh the table to update the Registered column
            refreshClassTable();
            // Also refresh workout plans in case this class has a workout plan
            loadWorkoutPlans();
        } else {
            showErrorAlert("Error", "Failed to register for class. Please try again.");
        }
    }
    
    /**
     * Refreshes the class table to update registration status.
     */
    private void refreshClassTable() {
        // Get the currently selected session ID to restore selection after refresh
        ClassSession selectedSession = classTable.getSelectionModel().getSelectedItem();
        long selectedId = selectedSession != null ? selectedSession.getId() : -1;
        
        // Reload data from database
        List<ClassSession> sessions = classScheduleService.getUpcomingClassSessions();
        
        // Clear and reload - this ensures all cell value factories are re-evaluated
        upcomingClasses.clear();
        upcomingClasses.addAll(sessions);
        
        // Force table refresh - this causes all cell value factories to be re-evaluated
        // The Registered column will check the database again for each row
        classTable.refresh();
        
        // Restore selection by finding the session with the same ID
        if (selectedId > 0) {
            for (int i = 0; i < upcomingClasses.size(); i++) {
                if (upcomingClasses.get(i).getId() == selectedId) {
                    classTable.getSelectionModel().select(i);
                    updateButtonStates(upcomingClasses.get(i));
                    break;
                }
            }
        } else {
            updateButtonStates(null);
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
                refreshClassTable(); // Refresh the table
                // Also refresh workout plans
                loadWorkoutPlans();
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
