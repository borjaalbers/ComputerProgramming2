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
import com.gymflow.service.FileImportExportService;
import com.gymflow.service.FileImportExportServiceImpl;
import com.gymflow.service.WorkoutService;
import com.gymflow.service.WorkoutServiceImpl;
import javafx.application.Platform;
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
import com.gymflow.controller.WorkoutPlanFormController;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private TableColumn<ClassSession, String> classRegisteredColumn;

    @FXML
    private TableColumn<ClassSession, String> classWorkoutPlanColumn;

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
    private final FileImportExportService fileService;

    private ObservableList<ClassSession> classSessions;
    private ObservableList<WorkoutPlan> workoutPlans;

    public TrainerDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.workoutService = new WorkoutServiceImpl();
        this.classScheduleService = new ClassScheduleServiceImpl();
        this.attendanceService = new AttendanceServiceImpl();
        this.fileService = new FileImportExportServiceImpl();
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
        classRegisteredColumn.setCellValueFactory(cellData -> {
            ClassSession session = cellData.getValue();
            if (session != null) {
                // Use a StringBinding that will update when the session changes
                javafx.beans.property.StringProperty prop = new javafx.beans.property.SimpleStringProperty();
                int registered = attendanceService.getRegisteredCount(session.getId());
                prop.set(registered + "/" + session.getCapacity());
                return prop;
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        classWorkoutPlanColumn.setCellValueFactory(cellData -> {
            ClassSession session = cellData.getValue();
            if (session != null && session.getWorkoutPlanId() != null) {
                // Get workout plan title
                String workoutTitle = getWorkoutPlanTitle(session.getWorkoutPlanId());
                return new javafx.beans.property.SimpleStringProperty(workoutTitle);
            }
            return new javafx.beans.property.SimpleStringProperty("None");
        });
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

    private void setupWorkoutTable() {
        workoutTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        workoutMemberColumn.setCellValueFactory(cellData -> {
            WorkoutPlan plan = cellData.getValue();
            if (plan != null) {
                // Get member name from database
                String memberName = getMemberName(plan.getMemberId());
                return new javafx.beans.property.SimpleStringProperty(memberName);
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
    
    /**
     * Gets the member's name from the database.
     */
    private String getMemberName(long memberId) {
        try {
            com.gymflow.dao.UserDao userDao = new com.gymflow.dao.UserDaoImpl();
            Optional<User> member = userDao.findById(memberId);
            if (member.isPresent()) {
                return member.get().getFullName();
            }
        } catch (Exception e) {
            System.err.println("Error getting member name: " + e.getMessage());
        }
        return "Member #" + memberId;
    }

    private void loadClassSessions() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            // Clear existing items first
            if (classSessions != null) {
                classSessions.clear();
            }
            
            // Reload from database
            List<ClassSession> sessions = classScheduleService.getClassSessionsByTrainer(currentUser.getId());
            classSessions = FXCollections.observableArrayList(sessions);
            classTable.setItems(classSessions);
            
            // Force table refresh
            classTable.refresh();
        } else {
            if (classSessions != null) {
                classSessions.clear();
            } else {
                classSessions = FXCollections.observableArrayList();
            }
            classTable.setItems(classSessions);
            classTable.refresh();
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
    private void handleEditClass() {
        ClassSession selectedSession = classTable.getSelectionModel().getSelectedItem();
        if (selectedSession == null) {
            showErrorAlert("No Selection", "Please select a class to edit");
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || selectedSession.getTrainerId() != currentUser.getId()) {
            showErrorAlert("Error", "You can only edit your own classes");
            return;
        }

        // Edit title
        TextInputDialog titleDialog = new TextInputDialog(selectedSession.getTitle());
        titleDialog.setTitle("Edit Class");
        titleDialog.setHeaderText("Enter new class title");
        titleDialog.setContentText("Class Title:");

        Optional<String> titleResult = titleDialog.showAndWait();
        if (titleResult.isPresent() && !titleResult.get().trim().isEmpty()) {
            String newTitle = titleResult.get().trim();
            
            // Edit capacity
            TextInputDialog capacityDialog = new TextInputDialog(String.valueOf(selectedSession.getCapacity()));
            capacityDialog.setTitle("Edit Class");
            capacityDialog.setHeaderText("Enter new capacity");
            capacityDialog.setContentText("Capacity:");

            Optional<String> capacityResult = capacityDialog.showAndWait();
            if (capacityResult.isPresent()) {
                try {
                    int newCapacity = Integer.parseInt(capacityResult.get().trim());
                    if (newCapacity <= 0) {
                        showErrorAlert("Error", "Capacity must be greater than 0");
                        return;
                    }

                    boolean success = classScheduleService.updateClassSession(
                        selectedSession.getId(), newTitle, null, newCapacity
                    );

                    if (success) {
                        showSuccessAlert("Success", "Class updated successfully!");
                        // Clear selection first
                        classTable.getSelectionModel().clearSelection();
                        // Reload from database
                        loadClassSessions();
                        // Force immediate refresh of all cells
                        javafx.application.Platform.runLater(() -> {
                            classTable.refresh();
                        });
                    } else {
                        showErrorAlert("Error", "Failed to update class");
                    }
                } catch (NumberFormatException e) {
                    showErrorAlert("Error", "Invalid capacity");
                }
            }
        }
    }

    @FXML
    private void handleAssignWorkoutPlan() {
        ClassSession selectedSession = classTable.getSelectionModel().getSelectedItem();
        if (selectedSession == null) {
            showErrorAlert("No Selection", "Please select a class to assign a workout plan");
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || selectedSession.getTrainerId() != currentUser.getId()) {
            showErrorAlert("Error", "You can only assign workout plans to your own classes");
            return;
        }

        // Get all workout plans for this trainer
        List<WorkoutPlan> trainerPlans = workoutService.getWorkoutPlansByTrainer(currentUser.getId());
        
        if (trainerPlans.isEmpty()) {
            showErrorAlert("No Workout Plans", "You need to create workout plans first before assigning them to classes.");
            return;
        }
        
        // Create a choice dialog
        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>();
        dialog.setTitle("Assign Workout Plan");
        dialog.setHeaderText("Select a workout plan to assign to '" + selectedSession.getTitle() + "'");
        dialog.setContentText("Workout Plan:");
        
        // Create list with "None" option first, then all workout plans
        ObservableList<String> options = FXCollections.observableArrayList();
        options.add("None (Remove Assignment)");
        for (WorkoutPlan plan : trainerPlans) {
            options.add(plan.getTitle() + " (ID: " + plan.getId() + ")");
        }
        
        dialog.getItems().addAll(options);
        
        // Set current selection if class already has a workout plan
        if (selectedSession.getWorkoutPlanId() != null) {
            Optional<WorkoutPlan> currentPlan = workoutService.getWorkoutPlanById(selectedSession.getWorkoutPlanId());
            if (currentPlan.isPresent()) {
                String currentTitle = currentPlan.get().getTitle() + " (ID: " + currentPlan.get().getId() + ")";
                if (options.contains(currentTitle)) {
                    dialog.setSelectedItem(currentTitle);
                }
            }
        } else {
            dialog.setSelectedItem("None (Remove Assignment)");
        }
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String selected = result.get();
            
            if ("None (Remove Assignment)".equals(selected)) {
                // Remove assignment
                boolean success = classScheduleService.assignWorkoutPlanToClass(
                    selectedSession.getId(), null
                );
                
                if (success) {
                    showSuccessAlert("Success", "Workout plan assignment removed from class");
                    loadClassSessions();
                } else {
                    showErrorAlert("Error", "Failed to remove workout plan assignment");
                }
            } else {
                // Extract workout plan ID from selection
                int idStart = selected.lastIndexOf("(ID: ") + 5;
                int idEnd = selected.lastIndexOf(")");
                if (idStart > 4 && idEnd > idStart) {
                    try {
                        long workoutPlanId = Long.parseLong(selected.substring(idStart, idEnd));
                        boolean success = classScheduleService.assignWorkoutPlanToClass(
                            selectedSession.getId(), workoutPlanId
                        );
                        
                        if (success) {
                            showSuccessAlert("Success", "Workout plan assigned to class successfully!");
                            loadClassSessions();
                        } else {
                            showErrorAlert("Error", "Failed to assign workout plan to class");
                        }
                    } catch (NumberFormatException e) {
                        showErrorAlert("Error", "Invalid workout plan selection");
                    }
                }
            }
        }
    }

    @FXML
    private void handleDeleteClass() {
        ClassSession selectedSession = classTable.getSelectionModel().getSelectedItem();
        if (selectedSession == null) {
            showErrorAlert("No Selection", "Please select a class to delete");
            return;
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || selectedSession.getTrainerId() != currentUser.getId()) {
            showErrorAlert("Error", "You can only delete your own classes");
            return;
        }

        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Class");
        confirmDialog.setContentText("Are you sure you want to delete '" + selectedSession.getTitle() + "'? This action cannot be undone.");

        Optional<javafx.scene.control.ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            boolean success = classScheduleService.deleteClassSession(selectedSession.getId());

            if (success) {
                showSuccessAlert("Success", "Class deleted successfully!");
                loadClassSessions(); // Refresh the table
            } else {
                showErrorAlert("Error", "Failed to delete class");
            }
        }
    }

    @FXML
    private void handleExportWorkoutPlans() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            showErrorAlert("Error", "No user logged in");
            return;
        }

        List<WorkoutPlan> plans = workoutService.getWorkoutPlansByTrainer(currentUser.getId());
        if (plans.isEmpty()) {
            showErrorAlert("No Data", "You have no workout plans to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Workout Plans");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("workout_plans_" + System.currentTimeMillis() + ".csv");

        Stage stage = (Stage) workoutTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                fileService.exportWorkoutTemplates(plans, file.getAbsolutePath());
                showSuccessAlert("Export Successful", 
                    String.format("Exported %d workout plan(s) to %s", plans.size(), file.getName()));
            } catch (com.gymflow.exception.FileOperationException | com.gymflow.exception.ValidationException e) {
                showErrorAlert("Export Error", e.getMessage());
            } catch (IOException e) {
                showErrorAlert("Export Error", "Failed to export workout plans: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleImportWorkoutPlans() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            showErrorAlert("Error", "No user logged in");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Workout Plans");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        Stage stage = (Stage) workoutTable.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                fileService.validateFile(file.getAbsolutePath());
                List<WorkoutPlan> importedPlans = fileService.importWorkoutTemplates(file.getAbsolutePath());
                
                if (importedPlans.isEmpty()) {
                    showErrorAlert("Import Error", "No valid workout plans found in the file");
                    return;
                }

                // Import each plan (they will have id=0, so they'll be created as new)
                int successCount = 0;
                int failCount = 0;

                for (WorkoutPlan plan : importedPlans) {
                    // Use the current trainer's ID for all imported plans
                    Optional<WorkoutPlan> created = workoutService.createWorkoutPlan(
                        plan.getMemberId(), currentUser.getId(), plan.getTitle(),
                        plan.getDescription(), plan.getDifficulty()
                    );
                    if (created.isPresent()) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                }

                String message = String.format("Imported %d workout plan(s) successfully", successCount);
                if (failCount > 0) {
                    message += String.format(", %d failed", failCount);
                }
                showSuccessAlert("Import Complete", message);
                loadWorkoutPlans(); // Refresh the table
            } catch (com.gymflow.exception.FileOperationException | com.gymflow.exception.ValidationException e) {
                showErrorAlert("Import Error", e.getMessage());
            } catch (FileNotFoundException e) {
                showErrorAlert("File Not Found", "The selected file could not be found");
            } catch (IllegalArgumentException e) {
                showErrorAlert("Invalid File", e.getMessage());
            } catch (IOException e) {
                showErrorAlert("Import Error", "Failed to import workout plans: " + e.getMessage());
                e.printStackTrace();
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

        try {
            // Load the form FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workout-plan-form.fxml"));
            DialogPane formPane = loader.load();
            WorkoutPlanFormController formController = loader.getController();

            // Create dialog
            javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
            dialog.setDialogPane(formPane);
            dialog.setTitle("Create Workout Plan");
            dialog.setResizable(true);

            // Set button types
            ButtonType createButtonType = new ButtonType("Create", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

            // Validate before closing
            javafx.scene.control.Button createButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(createButtonType);
            createButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (!formController.validate()) {
                    showErrorAlert("Validation Error", "Please select a member and enter a title.");
                    event.consume();
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == createButtonType) {
                // Get form data
                User selectedMember = formController.getSelectedMember();
                if (selectedMember == null) {
                    showErrorAlert("Error", "Please select a member");
                    return;
                }

                Optional<WorkoutPlan> created = workoutService.createWorkoutPlan(
                    selectedMember.getId(),
                    currentUser.getId(),
                    formController.getTitle(),
                    formController.getDescription(),
                    formController.getDifficulty(),
                    formController.getMuscleGroup(),
                    formController.getWorkoutType(),
                    formController.getDurationMinutes(),
                    formController.getEquipmentNeeded(),
                    formController.getTargetSets(),
                    formController.getTargetReps(),
                    formController.getRestSeconds()
                );

                if (created.isPresent()) {
                    showSuccessAlert("Success", "Workout plan created successfully!");
                    loadWorkoutPlans(); // Refresh the table
                } else {
                    showErrorAlert("Error", "Failed to create workout plan");
                }
            }
        } catch (IOException e) {
            showErrorAlert("Error", "Failed to load workout plan form: " + e.getMessage());
            e.printStackTrace();
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
