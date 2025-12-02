package com.gymflow.controller;

import com.gymflow.dao.UserDao;
import com.gymflow.dao.UserDaoImpl;
import com.gymflow.model.Role;
import com.gymflow.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Controller for the Workout Plan creation form dialog.
 */
public class WorkoutPlanFormController {
    
    @FXML
    private DialogPane dialogPane;
    
    @FXML
    private ComboBox<User> memberComboBox;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private ComboBox<String> difficultyComboBox;
    
    @FXML
    private ComboBox<String> workoutTypeComboBox;
    
    @FXML
    private ComboBox<String> muscleGroupComboBox;
    
    @FXML
    private TextField durationField;
    
    @FXML
    private TextField equipmentField;
    
    @FXML
    private TextField setsField;
    
    @FXML
    private TextField repsField;
    
    @FXML
    private TextField restField;
    
    private UserDao userDao;
    
    @FXML
    private void initialize() {
        userDao = new UserDaoImpl();
        setupMemberComboBox();
        setupDifficultyComboBox();
        setupWorkoutTypeComboBox();
        setupMuscleGroupComboBox();
    }
    
    private void setupMemberComboBox() {
        // Load all members
        try {
            List<User> members = userDao.findByRole(Role.MEMBER);
            ObservableList<User> memberList = FXCollections.observableArrayList(members);
            memberComboBox.setItems(memberList);
        } catch (com.gymflow.exception.DataAccessException e) {
            System.err.println("Error loading members: " + e.getMessage());
            memberComboBox.setItems(FXCollections.observableArrayList());
        }
        
        // Display member names in the combo box
        memberComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                if (user == null) {
                    return "";
                }
                return user.getFullName() + " (" + user.getUsername() + ")";
            }
            
            @Override
            public User fromString(String string) {
                return null; // Not needed for display-only
            }
        });
    }
    
    private void setupDifficultyComboBox() {
        ObservableList<String> difficulties = FXCollections.observableArrayList(
            "Beginner", "Intermediate", "Advanced", "Expert"
        );
        difficultyComboBox.setItems(difficulties);
        difficultyComboBox.setValue("Intermediate");
    }
    
    private void setupWorkoutTypeComboBox() {
        ObservableList<String> types = FXCollections.observableArrayList(
            "Strength Training", "Cardio", "Hybrid", "Flexibility", "HIIT", "Endurance", "Powerlifting", "Bodybuilding"
        );
        workoutTypeComboBox.setItems(types);
    }
    
    private void setupMuscleGroupComboBox() {
        ObservableList<String> muscleGroups = FXCollections.observableArrayList(
            "Full Body", "Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Cardio", "Upper Body", "Lower Body"
        );
        muscleGroupComboBox.setItems(muscleGroups);
    }
    
    public User getSelectedMember() {
        return memberComboBox.getValue();
    }
    
    public String getTitle() {
        return titleField.getText().trim();
    }
    
    public String getDescription() {
        return descriptionArea.getText().trim();
    }
    
    public String getDifficulty() {
        return difficultyComboBox.getValue();
    }
    
    public String getWorkoutType() {
        return workoutTypeComboBox.getValue();
    }
    
    public String getMuscleGroup() {
        return muscleGroupComboBox.getValue();
    }
    
    public Integer getDurationMinutes() {
        String text = durationField.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public String getEquipmentNeeded() {
        return equipmentField.getText().trim();
    }
    
    public Integer getTargetSets() {
        String text = setsField.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Integer getTargetReps() {
        String text = repsField.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Integer getRestSeconds() {
        String text = restField.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public boolean validate() {
        if (getSelectedMember() == null) {
            return false;
        }
        if (getTitle().isEmpty()) {
            return false;
        }
        return true;
    }
}

