package com.gymflow.controller;

import com.gymflow.model.ClassSession;
import com.gymflow.model.Member;
import com.gymflow.model.User;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.security.SessionManager;
import com.gymflow.service.AttendanceService;
import com.gymflow.service.AttendanceServiceImpl;
import com.gymflow.exception.DataAccessException;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
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
    private TableColumn<WorkoutPlan, String> workoutSourceColumn;

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
    private TableColumn<WorkoutPlan, String> workoutStatusColumn;

    @FXML
    private TableColumn<WorkoutPlan, String> workoutCreatedColumn;

    @FXML
    private javafx.scene.control.Button viewDetailsButton;

    @FXML
    private javafx.scene.control.Button markCompletedButton;

    @FXML
    private javafx.scene.control.Button deleteWorkoutButton;

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
    private final com.gymflow.service.WorkoutCompletionService completionService;

    private ObservableList<WorkoutPlan> workoutPlans;
    private ObservableList<ClassSession> upcomingClasses;
    // Map to track which class each workout came from
    private java.util.Map<Long, ClassSession> workoutToClassMap;

    public MemberDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.workoutService = new WorkoutServiceImpl();
        this.classScheduleService = new ClassScheduleServiceImpl();
        this.attendanceService = new AttendanceServiceImpl();
        this.completionService = new com.gymflow.service.WorkoutCompletionServiceImpl();
        this.workoutToClassMap = new java.util.HashMap<>();
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
            java.util.logging.Logger.getLogger(MemberDashboardController.class.getName()).log(java.util.logging.Level.SEVERE, "Error initializing MemberDashboardController", e);
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
            workoutCreatedColumn == null || workoutSourceColumn == null ||
            workoutStatusColumn == null) {
            java.util.logging.Logger.getLogger(MemberDashboardController.class.getName()).warning("Some workout table columns are null");
            return;
        }
        
        workoutTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Source column - shows which class the workout belongs to
        workoutSourceColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            if (plan != null) {
                ClassSession sourceClass = workoutToClassMap.get(plan.getId());
                if (sourceClass != null) {
                    return new javafx.beans.property.SimpleStringProperty("Class: " + sourceClass.getTitle());
                }
                return new javafx.beans.property.SimpleStringProperty("Direct Assignment");
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
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
        
        // Status column - shows if completed
        workoutStatusColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            User currentUser = sessionManager.getCurrentUser();
            if (plan != null && currentUser instanceof Member) {
                boolean completed = completionService.isCompleted(plan.getId(), currentUser.getId());
                javafx.beans.property.SimpleStringProperty prop = new javafx.beans.property.SimpleStringProperty(
                    completed ? "✓ Completed" : "Pending"
                );
                return prop;
            }
            return new javafx.beans.property.SimpleStringProperty("Pending");
        });
        
        workoutCreatedColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            if (plan != null && plan.getCreatedAt() != null) {
                String formatted = plan.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Enable row selection
        workoutTable.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);
        
        // Update button states based on selection
        workoutTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateWorkoutButtonStates(newSelection);
        });
        
        // Initialize button states
        updateWorkoutButtonStates(null);
    }
    
    /**
     * Updates the enabled state of workout action buttons based on selection.
     */
    private void updateWorkoutButtonStates(WorkoutPlan selectedPlan) {
        if (selectedPlan == null) {
            if (viewDetailsButton != null) viewDetailsButton.setDisable(true);
            if (markCompletedButton != null) markCompletedButton.setDisable(true);
            if (deleteWorkoutButton != null) deleteWorkoutButton.setDisable(true);
            return;
        }
        
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Member)) {
            if (viewDetailsButton != null) viewDetailsButton.setDisable(true);
            if (markCompletedButton != null) markCompletedButton.setDisable(true);
            if (deleteWorkoutButton != null) deleteWorkoutButton.setDisable(true);
            return;
        }
        
        // Check if workout is from a class
        ClassSession sourceClass = workoutToClassMap.get(selectedPlan.getId());
        boolean isFromClass = sourceClass != null;
        
        // Check if already completed
        boolean isCompleted = completionService.isCompleted(selectedPlan.getId(), currentUser.getId());
        
        if (viewDetailsButton != null) {
            viewDetailsButton.setDisable(false);
        }
        
        if (markCompletedButton != null) {
            markCompletedButton.setDisable(isCompleted);
            markCompletedButton.setText(isCompleted ? "Already Completed" : "Mark as Completed");
        }
        
        // Only allow deletion of direct assignments (not class workouts)
        if (deleteWorkoutButton != null) {
            deleteWorkoutButton.setDisable(isFromClass);
            if (isFromClass) {
                deleteWorkoutButton.setTooltip(new javafx.scene.control.Tooltip("Cannot delete workouts from classes"));
            }
        }
    }

    private void setupClassTable() {
        if (classTable == null || classNameColumn == null || classTrainerColumn == null ||
            classDateTimeColumn == null || classCapacityColumn == null ||
            classRegisteredColumn == null || classWorkoutPlanColumn == null) {
            java.util.logging.Logger.getLogger(MemberDashboardController.class.getName()).warning("Some class table columns are null");
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
                    if (session != null && currentUser instanceof Member) {
                        // Always check current registration status from database
                        boolean isRegistered = false;
                        try {
                            isRegistered = attendanceService.isRegisteredForClass(
                                session.getId(), currentUser.getId()
                            );
                        } catch (com.gymflow.exception.DataAccessException e) {
                            showErrorDialog("Database error while checking registration status: " + e.getMessage());
                        }
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
        
        boolean isRegistered = false;
        try {
            isRegistered = attendanceService.isRegisteredForClass(
                selectedSession.getId(), currentUser.getId()
            );
        } catch (com.gymflow.exception.DataAccessException e) {
            showErrorDialog("Database error while checking registration status: " + e.getMessage());
        }
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
            java.util.logging.Logger.getLogger(MemberDashboardController.class.getName()).log(java.util.logging.Level.SEVERE, "Error getting trainer name", e);
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
            java.util.logging.Logger.getLogger(MemberDashboardController.class.getName()).log(java.util.logging.Level.SEVERE, "Error getting workout plan title", e);
        }
        return "Unknown";
    }

    private void loadWorkoutPlans() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser instanceof Member) {
            // Clear the map
            workoutToClassMap.clear();
            
            // Get direct workout plans assigned to member
            List<WorkoutPlan> directPlans = java.util.Collections.emptyList();
            try {
                directPlans = workoutService.getWorkoutPlansForMember(currentUser.getId());
            } catch (com.gymflow.exception.DataAccessException e) {
                showErrorDialog("Database error while loading workout plans: " + e.getMessage());
            }
            
            // Get workout plans from registered classes and track their source
            List<WorkoutPlan> classPlans = new java.util.ArrayList<>();
            if (upcomingClasses != null) {
                for (ClassSession session : upcomingClasses) {
                    boolean registered = false;
                    try {
                        if (session != null && session.getWorkoutPlanId() != null && 
                            attendanceService.isRegisteredForClass(session.getId(), currentUser.getId())) {
                            registered = true;
                        }
                    } catch (com.gymflow.exception.DataAccessException e) {
                        showErrorDialog("Database error while checking class registration: " + e.getMessage());
                    }
                    if (registered) {
                        try {
                            Optional<WorkoutPlan> plan = workoutService.getWorkoutPlanById(session.getWorkoutPlanId());
                            if (plan.isPresent()) {
                                classPlans.add(plan.get());
                                // Track which class this workout came from
                                workoutToClassMap.put(plan.get().getId(), session);
                            }
                        } catch (com.gymflow.exception.DataAccessException e) {
                            showErrorDialog("Database error while loading workout plan: " + e.getMessage());
                        }
                    }
                }
            }
            // ...existing code...
        }
        // ...existing code...
    }


                    /**
                     * Shows an error dialog with the given message.
                     */
                    private void showErrorDialog(String message) {
                        javafx.application.Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
                            alert.setHeaderText("Error");
                            alert.showAndWait();
                        });
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
            if (workoutTable != null) {
                workoutTable.setItems(workoutPlans);
                workoutTable.refresh();
            }
        } else {
            workoutPlans = FXCollections.observableArrayList();
            if (workoutTable != null) {
                workoutTable.setItems(workoutPlans);
            }
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
        try {
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
                refreshClassTable();
                loadWorkoutPlans();
            } else {
                showErrorAlert("Error", "Failed to register for class. Please try again.");
            }
        } catch (DataAccessException dae) {
            showErrorAlert("Database Error", "A database error occurred: " + dae.getMessage());
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
        try {
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
                    refreshClassTable();
                    loadWorkoutPlans();
                } else {
                    showErrorAlert("Error", "Failed to unregister from class");
                }
            }
        } catch (DataAccessException dae) {
            showErrorAlert("Database Error", "A database error occurred: " + dae.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            sessionManager.logout();
            navigateToLogin();
        } catch (Exception e) {
            showErrorAlert("Logout Error", "An error occurred during logout: " + e.getMessage());
            java.util.logging.Logger.getLogger(MemberDashboardController.class.getName()).log(java.util.logging.Level.SEVERE, "Exception during logout", e);
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
            java.util.logging.Logger.getLogger(MemberDashboardController.class.getName()).log(java.util.logging.Level.SEVERE, "Exception during navigation to login", e);
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

    @FXML
    private void handleViewDetails() {
        WorkoutPlan selectedPlan = workoutTable.getSelectionModel().getSelectedItem();
        if (selectedPlan == null) {
            showErrorAlert("No Selection", "Please select a workout plan to view details");
            return;
        }

        // Get source class information
        ClassSession sourceClass = workoutToClassMap.get(selectedPlan.getId());
        String sourceInfo = sourceClass != null ? "Class: " + sourceClass.getTitle() : "Direct Assignment";

        // Check completion status
        User currentUser = sessionManager.getCurrentUser();
        boolean isCompleted = false;
        if (currentUser != null && currentUser instanceof Member) {
            isCompleted = completionService.isCompleted(selectedPlan.getId(), currentUser.getId());
        }

        // Create details dialog
        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(selectedPlan.getTitle()).append("\n\n");
        details.append("Source: ").append(sourceInfo).append("\n\n");
        if (selectedPlan.getDescription() != null && !selectedPlan.getDescription().isEmpty()) {
            details.append("Description: ").append(selectedPlan.getDescription()).append("\n\n");
        }
        details.append("Difficulty: ").append(selectedPlan.getDifficulty() != null ? selectedPlan.getDifficulty() : "Not specified").append("\n");
        details.append("Type: ").append(selectedPlan.getWorkoutType() != null ? selectedPlan.getWorkoutType() : "Not specified").append("\n");
        details.append("Muscle Group: ").append(selectedPlan.getMuscleGroup() != null ? selectedPlan.getMuscleGroup() : "Not specified").append("\n");
        if (selectedPlan.getDurationMinutes() != null) {
            details.append("Duration: ").append(selectedPlan.getDurationMinutes()).append(" minutes\n");
        }
        if (selectedPlan.getEquipmentNeeded() != null && !selectedPlan.getEquipmentNeeded().isEmpty()) {
            details.append("Equipment: ").append(selectedPlan.getEquipmentNeeded()).append("\n");
        }
        if (selectedPlan.getTargetSets() != null && selectedPlan.getTargetReps() != null) {
            details.append("Sets x Reps: ").append(selectedPlan.getTargetSets())
                   .append(" x ").append(selectedPlan.getTargetReps()).append("\n");
        }
        if (selectedPlan.getRestSeconds() != null) {
            details.append("Rest Time: ").append(selectedPlan.getRestSeconds()).append(" seconds\n");
        }
        details.append("\nStatus: ").append(isCompleted ? "✓ Completed" : "Pending");
        if (selectedPlan.getCreatedAt() != null) {
            details.append("\nCreated: ").append(selectedPlan.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Workout Plan Details");
        detailsAlert.setHeaderText("Workout Plan: " + selectedPlan.getTitle());
        detailsAlert.setContentText(details.toString());
        detailsAlert.getDialogPane().setPrefWidth(500);
        detailsAlert.showAndWait();
    }

    @FXML
    private void handleMarkCompleted() {
        WorkoutPlan selectedPlan = workoutTable.getSelectionModel().getSelectedItem();
        if (selectedPlan == null) {
            showErrorAlert("No Selection", "Please select a workout plan to mark as completed");
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Member)) {
            showErrorAlert("Error", "No member logged in");
            return;
        }

        // Check if already completed
        if (completionService.isCompleted(selectedPlan.getId(), currentUser.getId())) {
            showErrorAlert("Already Completed", "This workout plan is already marked as completed");
            return;
        }

        // Get source class if applicable
        ClassSession sourceClass = workoutToClassMap.get(selectedPlan.getId());
        Long classSessionId = sourceClass != null ? sourceClass.getId() : null;

        // Optional notes dialog
        TextInputDialog notesDialog = new TextInputDialog();
        notesDialog.setTitle("Mark Workout as Completed");
        notesDialog.setHeaderText("Mark '" + selectedPlan.getTitle() + "' as completed");
        notesDialog.setContentText("Notes (optional):");

        Optional<String> notesResult = notesDialog.showAndWait();
        String notes = notesResult.orElse("");

        // Mark as completed
        Optional<com.gymflow.model.WorkoutCompletion> result = completionService.markCompleted(
            selectedPlan.getId(), currentUser.getId(), classSessionId, notes
        );

        if (result.isPresent()) {
            showSuccessAlert("Success", "Workout plan marked as completed!");
            loadWorkoutPlans(); // Refresh to update status
        } else {
            showErrorAlert("Error", "Failed to mark workout as completed");
        }
    }

    @FXML
    private void handleDeleteWorkout() {
        WorkoutPlan selectedPlan = workoutTable.getSelectionModel().getSelectedItem();
        if (selectedPlan == null) {
            showErrorAlert("No Selection", "Please select a workout plan");
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || !(currentUser instanceof Member)) {
            showErrorAlert("Error", "No member logged in");
            return;
        }

        // Check if workout is from a class
        ClassSession sourceClass = workoutToClassMap.get(selectedPlan.getId());
        if (sourceClass != null) {
            showErrorAlert("Cannot Remove", "This workout plan is from a class. " +
                          "To remove it from your list, unregister from the class '" + sourceClass.getTitle() + "' in the Class Schedule tab.");
            return;
        }

        // For direct assignments, we can't actually delete the workout plan (it belongs to the trainer)
        // But we can show information about it
        showErrorAlert("Cannot Remove", "Workout plans are created by trainers and cannot be deleted by members. " +
                      "If you no longer need this workout plan, please contact your trainer to have it removed.");
    }
}
