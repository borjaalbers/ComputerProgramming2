package com.gymflow.controller;

import com.gymflow.dao.UserDao;
import com.gymflow.dao.UserDaoImpl;
import com.gymflow.model.AttendanceRecord;
import com.gymflow.model.Equipment;
import com.gymflow.model.Role;
import com.gymflow.model.User;
import com.gymflow.security.SessionManager;
import com.gymflow.service.AttendanceService;
import com.gymflow.service.AttendanceServiceImpl;
import com.gymflow.service.ClassScheduleService;
import com.gymflow.service.ClassScheduleServiceImpl;
import com.gymflow.service.EquipmentService;
import com.gymflow.service.EquipmentServiceImpl;
import com.gymflow.service.FileImportExportService;
import com.gymflow.service.FileImportExportServiceImpl;
import com.gymflow.service.UserService;
import com.gymflow.service.UserServiceImpl;
import com.gymflow.exception.ValidationException;
import com.gymflow.exception.DataAccessException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;

import com.gymflow.exception.FileOperationException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Administrator Dashboard.
 * Displays admin-specific information and system management functionality.
 */
public class AdminDashboardController {

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
    private Label totalMembersLabel;

    @FXML
    private Label totalTrainersLabel;

    @FXML
    private Label activeClassesLabel;

    @FXML
    private Label equipmentCountLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<Equipment> equipmentTable;

    @FXML
    private TableColumn<Equipment, String> equipmentNameColumn;

    @FXML
    private TableColumn<Equipment, String> equipmentStatusColumn;

    @FXML
    private TableColumn<Equipment, String> equipmentLastServiceColumn;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> createdColumn;

    @FXML
    private TableColumn<User, String> actionsColumn;

    @FXML
    private Button addUserButton;

    @FXML
    private Button refreshUsersButton;

    private final SessionManager sessionManager;
    private final UserDao userDao;
    private final UserService userService;
    private final ClassScheduleService classScheduleService;
    private final EquipmentService equipmentService;
    private final AttendanceService attendanceService;
    private final FileImportExportService fileService;

    private ObservableList<Equipment> equipmentList;
    private ObservableList<User> userList;

    public AdminDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.userDao = new UserDaoImpl();
        this.userService = new UserServiceImpl();
        this.classScheduleService = new ClassScheduleServiceImpl();
        this.equipmentService = new EquipmentServiceImpl();
        this.attendanceService = new AttendanceServiceImpl();
        this.fileService = new FileImportExportServiceImpl();
    }

    @FXML
    private void initialize() {
        loadUserInfo();
        setupEquipmentTable();
        setupUserTable();
        loadSystemStats();
        loadEquipment();
        loadUsers();
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

    private void setupEquipmentTable() {
        equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        equipmentStatusColumn.setCellValueFactory(cellData -> {
            Equipment equipment = cellData.getValue();
            if (equipment != null) {
                return new javafx.beans.property.SimpleStringProperty(equipment.getStatus().name());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        equipmentLastServiceColumn.setCellValueFactory(cellData -> {
            Equipment equipment = cellData.getValue();
            if (equipment != null && equipment.getLastServiceDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(equipment.getLastServiceDate().toString());
            }
            return new javafx.beans.property.SimpleStringProperty("Never");
        });
    }

    private void loadSystemStats() {
        // Load actual statistics from database
        try {
            int members = userDao.countByRole(Role.MEMBER);
            int trainers = userDao.countByRole(Role.TRAINER);
            int upcomingClasses = classScheduleService.getUpcomingClassSessions().size();
            int equipmentCount = equipmentService.getAllEquipment().size();

            totalMembersLabel.setText(String.valueOf(members));
            totalTrainersLabel.setText(String.valueOf(trainers));
            activeClassesLabel.setText(String.valueOf(upcomingClasses));
            equipmentCountLabel.setText(String.valueOf(equipmentCount));
        } catch (com.gymflow.exception.DataAccessException e) {
            System.err.println("Error loading system stats: " + e.getMessage());
            // Set default values on error
            totalMembersLabel.setText("0");
            totalTrainersLabel.setText("0");
            activeClassesLabel.setText("0");
            equipmentCountLabel.setText("0");
        }
    }

    private void loadEquipment() {
        equipmentList = FXCollections.observableArrayList(
            equipmentService.getAllEquipment()
        );
        equipmentTable.setItems(equipmentList);
    }

    private void setupUserTable() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user != null) {
                return new javafx.beans.property.SimpleStringProperty(user.getRole().name());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        createdColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user != null && user.getCreatedAt() != null) {
                String formatted = user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Actions column with Edit and Delete buttons
        actionsColumn.setCellFactory(column -> new javafx.scene.control.TableCell<User, String>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10;");
                
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });
                
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    // Prevent deleting yourself
                    User currentUser = sessionManager.getCurrentUser();
                    boolean isCurrentUser = currentUser != null && currentUser.getId() == user.getId();
                    
                    deleteButton.setDisable(isCurrentUser);
                    if (isCurrentUser) {
                        deleteButton.setTooltip(new javafx.scene.control.Tooltip("Cannot delete your own account"));
                    }
                    
                    HBox hbox = new HBox(5);
                    hbox.getChildren().addAll(editButton, deleteButton);
                    setGraphic(hbox);
                }
            }
        });

        // Enable row selection
        userTable.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);
    }

    private void loadUsers() {
        try {
            userList = FXCollections.observableArrayList(userService.getAllUsers());
            userTable.setItems(userList);
        } catch (DataAccessException e) {
            showErrorAlert("Error", "Failed to load users: " + e.getMessage());
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddUser() {
        // Create a dialog for adding a new user
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Enter user information");

        // Set button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create form fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        ComboBox<Role> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(Role.values());
        roleComboBox.setValue(Role.MEMBER);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleComboBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Request focus on username field
        Platform.runLater(() -> usernameField.requestFocus());

        // Convert result to User when create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    Optional<User> created = userService.createUser(
                        usernameField.getText(),
                        passwordField.getText(),
                        fullNameField.getText(),
                        emailField.getText(),
                        roleComboBox.getValue()
                    );
                    return created.orElse(null);
                } catch (ValidationException | DataAccessException e) {
                    showErrorAlert("Error", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            showSuccessAlert("Success", "User created successfully!");
            loadUsers();
            loadSystemStats(); // Refresh statistics
        } else if (result.isPresent() && result.get() == null) {
            // User creation failed (likely duplicate username)
            showErrorAlert("Error", "Failed to create user. Username may already exist.");
        }
    }

    @FXML
    private void handleRefreshUsers() {
        loadUsers();
        showSuccessAlert("Refresh", "User list refreshed");
    }

    private void handleEditUser(User user) {
        if (user == null) {
            return;
        }

        // Create a dialog for editing user
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user information for: " + user.getUsername());

        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields (username cannot be changed)
        TextField fullNameField = new TextField(user.getFullName());
        fullNameField.setPromptText("Full Name");
        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Email");
        ComboBox<Role> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(Role.values());
        roleComboBox.setValue(user.getRole());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(new Label(user.getUsername() + " (cannot be changed)"), 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Request focus on full name field
        Platform.runLater(() -> fullNameField.requestFocus());

        // Convert result when save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    boolean success = userService.updateUser(
                        user.getId(),
                        fullNameField.getText(),
                        emailField.getText(),
                        roleComboBox.getValue()
                    );
                    return success;
                } catch (ValidationException | DataAccessException e) {
                    showErrorAlert("Error", e.getMessage());
                    return false;
                }
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            showSuccessAlert("Success", "User updated successfully!");
            loadUsers();
            loadSystemStats(); // Refresh statistics
        }
    }

    private void handleDeleteUser(User user) {
        if (user == null) {
            return;
        }

        // Prevent deleting yourself
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getId() == user.getId()) {
            showErrorAlert("Error", "You cannot delete your own account");
            return;
        }

        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete User");
        confirmDialog.setContentText("Are you sure you want to delete user '" + user.getUsername() + "' (" + user.getFullName() + ")?\n\nThis action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = userService.deleteUser(user.getId());
                if (success) {
                    showSuccessAlert("Success", "User deleted successfully!");
                    loadUsers();
                    loadSystemStats(); // Refresh statistics
                } else {
                    showErrorAlert("Error", "Failed to delete user");
                }
            } catch (DataAccessException e) {
                showErrorAlert("Error", "Failed to delete user: " + e.getMessage());
                System.err.println("Error deleting user: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddEquipment() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Equipment");
        dialog.setHeaderText("Enter equipment details");
        dialog.setContentText("Equipment Name:");

        Optional<String> nameResult = dialog.showAndWait();
        if (nameResult.isPresent() && !nameResult.get().trim().isEmpty()) {
            String name = nameResult.get().trim();

            Optional<Equipment> created = equipmentService.createEquipment(
                name, com.gymflow.model.EquipmentStatus.AVAILABLE, null
            );

            if (created.isPresent()) {
                showSuccessAlert("Success", "Equipment added successfully!");
                loadEquipment(); // Refresh the table
                loadSystemStats(); // Refresh statistics
            } else {
                showErrorAlert("Error", "Failed to add equipment");
            }
        }
    }

    @FXML
    private void handleExportAttendanceReport() {
        // Get all attendance records
        List<AttendanceRecord> allRecords = attendanceService.getAllAttendanceRecords();

        if (allRecords.isEmpty()) {
            showErrorAlert("No Data", "No attendance records found to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("attendance_report_" + System.currentTimeMillis() + ".csv");

        Stage stage = (Stage) equipmentTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // Check for file overwrite
            if (file.exists()) {
                Alert overwriteAlert = new Alert(Alert.AlertType.CONFIRMATION);
                overwriteAlert.setTitle("File Exists");
                overwriteAlert.setHeaderText("File already exists");
                overwriteAlert.setContentText("The file " + file.getName() + " already exists. Do you want to overwrite it?");
                Optional<ButtonType> result = overwriteAlert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return; // User cancelled
                }
            }

            try {
                // Enrich data with member and class names
                java.util.Map<Long, String> memberNameMap = new java.util.HashMap<>();
                java.util.Map<Long, String> classNameMap = new java.util.HashMap<>();

                // Build member name map
                for (AttendanceRecord record : allRecords) {
                    if (!memberNameMap.containsKey(record.getMemberId())) {
                        try {
                            Optional<User> member = userDao.findById(record.getMemberId());
                            if (member.isPresent()) {
                                memberNameMap.put(record.getMemberId(), member.get().getFullName());
                            }
                        } catch (com.gymflow.exception.DataAccessException e) {
                            System.err.println("Error loading member name: " + e.getMessage());
                            memberNameMap.put(record.getMemberId(), "Unknown Member");
                        }
                    }
                    if (!classNameMap.containsKey(record.getSessionId())) {
                        Optional<com.gymflow.model.ClassSession> session = classScheduleService.getClassSessionById(record.getSessionId());
                        if (session.isPresent()) {
                            classNameMap.put(record.getSessionId(), session.get().getTitle());
                        }
                    }
                }

                fileService.exportAttendanceReport(allRecords, file.getAbsolutePath(), memberNameMap, classNameMap);
                showSuccessAlert("Export Successful", 
                    String.format("Exported %d attendance record(s) to %s", allRecords.size(), file.getName()));
            } catch (FileOperationException e) {
                showErrorAlert("Export Error", "Failed to export attendance report: " + e.getMessage());
                System.err.println("File operation error: " + e.getMessage());
                e.printStackTrace();
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
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
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
