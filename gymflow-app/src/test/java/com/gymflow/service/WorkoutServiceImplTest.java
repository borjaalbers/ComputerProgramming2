package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.exception.DataAccessException;
import com.gymflow.model.WorkoutPlan;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WorkoutServiceImplTest {
    private static DatabaseConnection dbConnection;
    private WorkoutServiceImpl workoutService;

    // 1. Initialize DB Configuration ONCE
    @BeforeAll
    static void initDbConfig() {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:testdb_workout_service;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dbConnection = DatabaseConnection.getInstance();
    }

    // 2. Set up Schema and Data BEFORE EVERY TEST
    @BeforeEach
    void setUp() throws Exception {
        workoutService = new WorkoutServiceImpl();

        // Get a fresh connection for this specific test setup
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Re-create schema to ensure clean state
            stmt.execute("DROP TABLE IF EXISTS workout_plans");

            stmt.execute("""
                CREATE TABLE workout_plans (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    member_id INT NOT NULL,
                    trainer_id INT NOT NULL,
                    title VARCHAR(100) NOT NULL,
                    description TEXT,
                    difficulty VARCHAR(50),
                    muscle_group VARCHAR(50),
                    workout_type VARCHAR(50),
                    duration_minutes INT,
                    equipment_needed VARCHAR(100),
                    target_sets INT,
                    target_reps INT,
                    rest_seconds INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Insert fresh test data
            stmt.execute("""
                INSERT INTO workout_plans (
                    id, member_id, trainer_id, title, description, difficulty, 
                    muscle_group, workout_type, duration_minutes, target_sets, target_reps
                )
                VALUES (
                    1, 1, 2, 'Test Plan', 'A test workout plan', 'Beginner', 
                    'Full Body', 'Strength', 60, 3, 10
                )
            """);
        }
    }

    @Test
    void testGetWorkoutPlanById_Existing() {
        Optional<WorkoutPlan> result = workoutService.getWorkoutPlanById(1);
        assertTrue(result.isPresent(), "Should find the plan with ID 1");
        assertEquals("Test Plan", result.get().getTitle());
    }

    @Test
    void testGetWorkoutPlanById_NonExistent() {
        Optional<WorkoutPlan> result = workoutService.getWorkoutPlanById(999);
        assertFalse(result.isPresent());
    }

    @Test
    void testCreateWorkoutPlan_Valid() {
        Optional<WorkoutPlan> created = workoutService.createWorkoutPlan(1, 2, "New Plan", "Desc", "Intermediate");
        assertTrue(created.isPresent());
        assertEquals("New Plan", created.get().getTitle());
    }

    @Test
    void testCreateWorkoutPlan_InvalidTitle() {
        assertThrows(IllegalArgumentException.class, () ->
                workoutService.createWorkoutPlan(1, 2, "", "Desc", "Intermediate")
        );
    }

    @Test
    void testDeleteWorkoutPlan_Existing() {
        boolean deleted = workoutService.deleteWorkoutPlan(1);
        assertTrue(deleted, "Should successfully delete existing plan");

        Optional<WorkoutPlan> check = workoutService.getWorkoutPlanById(1);
        assertFalse(check.isPresent(), "Plan should be gone after deletion");
    }

    @Test
    void testDeleteWorkoutPlan_NonExistent() {
        assertThrows(DataAccessException.class, () ->
                workoutService.deleteWorkoutPlan(999)
        );
    }
}