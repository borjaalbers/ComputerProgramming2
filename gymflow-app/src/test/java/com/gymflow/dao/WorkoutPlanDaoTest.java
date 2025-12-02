package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.Role;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.security.PasswordHasher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for WorkoutPlanDao using H2 in-memory database.
 */
class WorkoutPlanDaoTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection;
    private WorkoutPlanDao workoutPlanDao;
    private long memberId;
    private long trainerId;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:workoutdao_testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
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
        workoutPlanDao = new WorkoutPlanDaoImpl();
        memberId = 1;
        trainerId = 2;
    }

    @Test
    void testCreate_WithAllFields_Success() {
        WorkoutPlan plan = new WorkoutPlan(0, memberId, trainerId, "Test Plan",
            "Test Description", "Intermediate", "Chest", "Strength Training",
            45, "Dumbbells", 3, 12, 60, LocalDateTime.now());

        Optional<WorkoutPlan> result = workoutPlanDao.create(plan);

        assertTrue(result.isPresent());
        WorkoutPlan created = result.get();
        assertTrue(created.getId() > 0, "Should have generated ID");
        assertEquals("Test Plan", created.getTitle());
        assertEquals("Chest", created.getMuscleGroup());
        assertEquals(45, created.getDurationMinutes());
    }

    @Test
    void testFindById_ExistingPlan_ReturnsPlan() {
        WorkoutPlan plan = new WorkoutPlan(0, memberId, trainerId, "Find Me",
            "Test", "Beginner", null, null, null, null, null, null, null, LocalDateTime.now());
        Optional<WorkoutPlan> created = workoutPlanDao.create(plan);
        assertTrue(created.isPresent());
        long planId = created.get().getId();

        Optional<WorkoutPlan> found = workoutPlanDao.findById(planId);

        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getTitle());
    }

    @Test
    void testFindById_NonExistent_ReturnsEmpty() {
        Optional<WorkoutPlan> result = workoutPlanDao.findById(99999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByMemberId_Success() {
        WorkoutPlan plan1 = new WorkoutPlan(0, memberId, trainerId, "Plan 1",
            "Test", "Beginner", null, null, null, null, null, null, null, LocalDateTime.now());
        WorkoutPlan plan2 = new WorkoutPlan(0, memberId, trainerId, "Plan 2",
            "Test", "Intermediate", null, null, null, null, null, null, null, LocalDateTime.now());
        workoutPlanDao.create(plan1);
        workoutPlanDao.create(plan2);

        List<WorkoutPlan> plans = workoutPlanDao.findByMemberId(memberId);

        assertTrue(plans.size() >= 2);
        assertTrue(plans.stream().anyMatch(p -> p.getTitle().equals("Plan 1")));
        assertTrue(plans.stream().anyMatch(p -> p.getTitle().equals("Plan 2")));
    }

    @Test
    void testFindByTrainerId_Success() {
        WorkoutPlan plan = new WorkoutPlan(0, memberId, trainerId, "Trainer Plan",
            "Test", "Advanced", null, null, null, null, null, null, null, LocalDateTime.now());
        workoutPlanDao.create(plan);

        List<WorkoutPlan> plans = workoutPlanDao.findByTrainerId(trainerId);

        assertFalse(plans.isEmpty());
        assertTrue(plans.stream().anyMatch(p -> p.getTitle().equals("Trainer Plan")));
    }

    @Test
    void testUpdate_Success() {
        WorkoutPlan plan = new WorkoutPlan(0, memberId, trainerId, "Original",
            "Test", "Beginner", null, null, null, null, null, null, null, LocalDateTime.now());
        Optional<WorkoutPlan> created = workoutPlanDao.create(plan);
        assertTrue(created.isPresent());
        WorkoutPlan toUpdate = created.get();
        toUpdate.setTitle("Updated");
        toUpdate.setDifficulty("Advanced");

        boolean updated = workoutPlanDao.update(toUpdate);

        assertTrue(updated);
        Optional<WorkoutPlan> found = workoutPlanDao.findById(toUpdate.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getTitle());
        assertEquals("Advanced", found.get().getDifficulty());
    }

    @Test
    void testDelete_Success() {
        WorkoutPlan plan = new WorkoutPlan(0, memberId, trainerId, "To Delete",
            "Test", "Beginner", null, null, null, null, null, null, null, LocalDateTime.now());
        Optional<WorkoutPlan> created = workoutPlanDao.create(plan);
        assertTrue(created.isPresent());
        long planId = created.get().getId();

        boolean deleted = workoutPlanDao.delete(planId);

        assertTrue(deleted);
        Optional<WorkoutPlan> found = workoutPlanDao.findById(planId);
        assertFalse(found.isPresent(), "Plan should be deleted");
    }
}

