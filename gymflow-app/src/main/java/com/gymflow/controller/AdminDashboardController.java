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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

    private final SessionManager sessionManager;
    private final UserDao userDao;
    private final ClassScheduleService classScheduleService;
    private final EquipmentService equipmentService;
    private final AttendanceService attendanceService;
    private final FileImportExportService fileService;

    private ObservableList<Equipment> equipmentList;

    public AdminDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.userDao = new UserDaoImpl();
        this.classScheduleService = new ClassScheduleServiceImpl();
        this.equipmentService = new EquipmentServiceImpl();
        this.attendanceService = new AttendanceServiceImpl();
        this.fileService = new FileImportExportServiceImpl();
    }

    @FXML
    private void initialize() {
        loadUserInfo();
        setupEquipmentTable();
        loadSystemStats();
        loadEquipment();
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
        int members = userDao.countByRole(Role.MEMBER);
        int trainers = userDao.countByRole(Role.TRAINER);
        int upcomingClasses = classScheduleService.getUpcomingClassSessions().size();
        int equipmentCount = equipmentService.getAllEquipment().size();

        totalMembersLabel.setText(String.valueOf(members));
        totalTrainersLabel.setText(String.valueOf(trainers));
        activeClassesLabel.setText(String.valueOf(upcomingClasses));
        equipmentCountLabel.setText(String.valueOf(equipmentCount));
    }

    private void loadEquipment() {
        equipmentList = FXCollections.observableArrayList(
            equipmentService.getAllEquipment()
        );
        equipmentTable.setItems(equipmentList);
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
            try {
                fileService.exportAttendanceReport(allRecords, file.getAbsolutePath());
                showSuccessAlert("Export Successful", 
                    String.format("Exported %d attendance record(s) to %s", allRecords.size(), file.getName()));
            } catch (IOException e) {
                showErrorAlert("Export Error", "Failed to export attendance report: " + e.getMessage());
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
