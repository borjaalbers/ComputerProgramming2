package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.Role;
import com.gymflow.model.User;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.security.PasswordHasher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for WorkoutService using H2 in-memory database.
 */
class WorkoutServiceTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection;
    private WorkoutService workoutService;
    private long memberId;
    private long trainerId;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:workout_testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dbConnection = DatabaseConnection.getInstance();
        testConnection = dbConnection.getConnection();

        try (Statement stmt = testConnection.createStatement()) {
            // Create tables
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) UNIQUE NOT NULL
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    role_id INT NOT NULL,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    full_name VARCHAR(150) NOT NULL,
                    email VARCHAR(150) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (role_id) REFERENCES roles(id)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS workout_plans (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    member_id INT,
                    trainer_id INT,
                    title VARCHAR(150) NOT NULL,
                    description TEXT,
                    difficulty VARCHAR(50),
                    muscle_group VARCHAR(100),
                    workout_type VARCHAR(50),
                    duration_minutes INT,
                    equipment_needed TEXT,
                    target_sets INT,
                    target_reps INT,
                    rest_seconds INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (member_id) REFERENCES users(id),
                    FOREIGN KEY (trainer_id) REFERENCES users(id)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS class_sessions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    trainer_id INT,
                    title VARCHAR(150) NOT NULL,
                    schedule_timestamp TIMESTAMP NOT NULL,
                    capacity INT DEFAULT 10,
                    workout_plan_id INT,
                    FOREIGN KEY (trainer_id) REFERENCES users(id),
                    FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS attendance_records (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    session_id INT,
                    member_id INT,
                    attended BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (session_id) REFERENCES class_sessions(id),
                    FOREIGN KEY (member_id) REFERENCES users(id)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS workout_completions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    workout_plan_id INT,
                    member_id INT,
                    class_session_id INT,
                    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    notes TEXT,
                    FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id),
                    FOREIGN KEY (member_id) REFERENCES users(id),
                    FOREIGN KEY (class_session_id) REFERENCES class_sessions(id)
                )
                """);

            // Clear data (delete in order to respect foreign keys)
            stmt.execute("DELETE FROM workout_completions");
            stmt.execute("DELETE FROM attendance_records");
            stmt.execute("DELETE FROM class_sessions");
            stmt.execute("DELETE FROM workout_plans");
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM roles");

            // Insert roles
            stmt.execute("INSERT INTO roles (id, name) VALUES (1, 'MEMBER')");
            stmt.execute("INSERT INTO roles (id, name) VALUES (2, 'TRAINER')");

            // Insert test users
            String passwordHash = PasswordHasher.sha256("password123");
            stmt.execute(String.format("""
                INSERT INTO users (id, role_id, username, password_hash, full_name, email) 
                VALUES (1, 1, 'testmember', '%s', 'Test Member', 'member@test.com')
                """, passwordHash));
            stmt.execute(String.format("""
                INSERT INTO users (id, role_id, username, password_hash, full_name, email) 
                VALUES (2, 2, 'testtrainer', '%s', 'Test Trainer', 'trainer@test.com')
                """, passwordHash));
        }
        testConnection.setAutoCommit(true);
    }

    @BeforeEach
    void setUp() {
        workoutService = new WorkoutServiceImpl();
        memberId = 1;
        trainerId = 2;
    }

    @Test
    void testCreateWorkoutPlan_WithAllFields_Success() {
        Optional<WorkoutPlan> result = workoutService.createWorkoutPlan(
            memberId, trainerId, "Full Body Workout", "Complete workout",
            "Intermediate", "Full Body", "Strength Training", 45,
            "Dumbbells", 3, 12, 60
        );

        assertTrue(result.isPresent(), "Workout plan should be created");
        WorkoutPlan plan = result.get();
        assertEquals("Full Body Workout", plan.getTitle());
        assertEquals("Full Body", plan.getMuscleGroup());
        assertEquals("Strength Training", plan.getWorkoutType());
        assertEquals(45, plan.getDurationMinutes());
        assertEquals(3, plan.getTargetSets());
        assertEquals(12, plan.getTargetReps());
        assertEquals(60, plan.getRestSeconds());
    }

    @Test
    void testCreateWorkoutPlan_BasicFields_Success() {
        Optional<WorkoutPlan> result = workoutService.createWorkoutPlan(
            memberId, trainerId, "Basic Workout", "Simple workout", "Beginner"
        );

        assertTrue(result.isPresent());
        assertEquals("Basic Workout", result.get().getTitle());
        assertEquals("Beginner", result.get().getDifficulty());
    }

    @Test
    void testCreateWorkoutPlan_EmptyTitle_ReturnsEmpty() {
        Optional<WorkoutPlan> result = workoutService.createWorkoutPlan(
            memberId, trainerId, "", "Description", "Beginner"
        );

        assertFalse(result.isPresent(), "Should not create workout with empty title");
    }

    @Test
    void testGetWorkoutPlansForMember_Success() {
        // Create a workout plan
        workoutService.createWorkoutPlan(memberId, trainerId, "Member Workout",
            "Test", "Intermediate", "Chest", "Strength Training", 30, null, 3, 10, 60);

        List<WorkoutPlan> plans = workoutService.getWorkoutPlansForMember(memberId);

        assertFalse(plans.isEmpty());
        assertTrue(plans.stream().anyMatch(p -> p.getTitle().equals("Member Workout")));
    }

    @Test
    void testGetWorkoutPlansByTrainer_Success() {
        // Create a workout plan
        workoutService.createWorkoutPlan(memberId, trainerId, "Trainer Workout",
            "Test", "Advanced", "Legs", "Cardio", 20, null, null, null, null);

        List<WorkoutPlan> plans = workoutService.getWorkoutPlansByTrainer(trainerId);

        assertFalse(plans.isEmpty());
        assertTrue(plans.stream().anyMatch(p -> p.getTitle().equals("Trainer Workout")));
    }

    @Test
    void testGetWorkoutPlanById_Success() {
        Optional<WorkoutPlan> created = workoutService.createWorkoutPlan(
            memberId, trainerId, "Find Me", "Test", "Beginner"
        );
        assertTrue(created.isPresent());
        long planId = created.get().getId();

        Optional<WorkoutPlan> found = workoutService.getWorkoutPlanById(planId);

        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getTitle());
    }

    @Test
    void testGetWorkoutPlanById_NonExistent_ReturnsEmpty() {
        Optional<WorkoutPlan> result = workoutService.getWorkoutPlanById(99999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateWorkoutPlan_Success() {
        Optional<WorkoutPlan> created = workoutService.createWorkoutPlan(
            memberId, trainerId, "Original Title", "Original Desc", "Beginner"
        );
        assertTrue(created.isPresent());
        long planId = created.get().getId();

        boolean updated = workoutService.updateWorkoutPlan(planId, "Updated Title",
            "Updated Desc", "Advanced");

        assertTrue(updated);
        Optional<WorkoutPlan> updatedPlan = workoutService.getWorkoutPlanById(planId);
        assertTrue(updatedPlan.isPresent());
        assertEquals("Updated Title", updatedPlan.get().getTitle());
        assertEquals("Advanced", updatedPlan.get().getDifficulty());
    }

    @Test
    void testDeleteWorkoutPlan_Success() {
        Optional<WorkoutPlan> created = workoutService.createWorkoutPlan(
            memberId, trainerId, "To Delete", "Test", "Beginner"
        );
        assertTrue(created.isPresent());
        long planId = created.get().getId();

        boolean deleted = workoutService.deleteWorkoutPlan(planId);

        assertTrue(deleted);
        Optional<WorkoutPlan> found = workoutService.getWorkoutPlanById(planId);
        assertFalse(found.isPresent(), "Workout plan should be deleted");
    }
}

