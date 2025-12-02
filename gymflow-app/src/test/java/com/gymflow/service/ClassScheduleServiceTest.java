package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.ClassSession;
import com.gymflow.model.Role;
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
 * Integration test for ClassScheduleService using H2 in-memory database.
 */
class ClassScheduleServiceTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection;
    private ClassScheduleService classScheduleService;
    private long trainerId;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:class_testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
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
        classScheduleService = new ClassScheduleServiceImpl();
        trainerId = 2;
    }

    @Test
    void testCreateClassSession_Success() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        Optional<ClassSession> result = classScheduleService.createClassSession(
            trainerId, "Morning Yoga", futureTime, 20
        );

        assertTrue(result.isPresent(), "Class session should be created");
        ClassSession session = result.get();
        assertEquals("Morning Yoga", session.getTitle());
        assertEquals(20, session.getCapacity());
        assertEquals(trainerId, session.getTrainerId());
    }

    @Test
    void testCreateClassSession_EmptyTitle_ReturnsEmpty() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        Optional<ClassSession> result = classScheduleService.createClassSession(
            trainerId, "", futureTime, 10
        );

        assertFalse(result.isPresent(), "Should not create class with empty title");
    }

    @Test
    void testGetClassSessionsByTrainer_Success() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        classScheduleService.createClassSession(trainerId, "Test Class", futureTime, 15);

        List<ClassSession> sessions = classScheduleService.getClassSessionsByTrainer(trainerId);

        assertFalse(sessions.isEmpty());
        assertTrue(sessions.stream().anyMatch(s -> s.getTitle().equals("Test Class")));
    }

    @Test
    void testGetUpcomingClassSessions_Success() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        classScheduleService.createClassSession(trainerId, "Future Class", futureTime, 10);

        List<ClassSession> upcoming = classScheduleService.getUpcomingClassSessions();

        assertFalse(upcoming.isEmpty());
        assertTrue(upcoming.stream().anyMatch(s -> s.getTitle().equals("Future Class")));
    }

    @Test
    void testGetClassSessionById_Success() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        Optional<ClassSession> created = classScheduleService.createClassSession(
            trainerId, "Find Me", futureTime, 10
        );
        assertTrue(created.isPresent());
        long sessionId = created.get().getId();

        Optional<ClassSession> found = classScheduleService.getClassSessionById(sessionId);

        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getTitle());
    }

    @Test
    void testUpdateClassSession_Success() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        Optional<ClassSession> created = classScheduleService.createClassSession(
            trainerId, "Original Title", futureTime, 10
        );
        assertTrue(created.isPresent());
        long sessionId = created.get().getId();

        LocalDateTime newTime = LocalDateTime.now().plusDays(2);
        boolean updated = classScheduleService.updateClassSession(
            sessionId, "Updated Title", newTime, 25
        );

        assertTrue(updated);
        Optional<ClassSession> updatedSession = classScheduleService.getClassSessionById(sessionId);
        assertTrue(updatedSession.isPresent());
        assertEquals("Updated Title", updatedSession.get().getTitle());
        assertEquals(25, updatedSession.get().getCapacity());
    }

    @Test
    void testDeleteClassSession_Success() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        Optional<ClassSession> created = classScheduleService.createClassSession(
            trainerId, "To Delete", futureTime, 10
        );
        assertTrue(created.isPresent());
        long sessionId = created.get().getId();

        boolean deleted = classScheduleService.deleteClassSession(sessionId);

        assertTrue(deleted);
        Optional<ClassSession> found = classScheduleService.getClassSessionById(sessionId);
        assertFalse(found.isPresent(), "Class session should be deleted");
    }

    @Test
    void testAssignWorkoutPlanToClass_Success() throws Exception {
        // Create a workout plan first
        WorkoutService workoutService = new WorkoutServiceImpl();
        long memberId = 1; // Use the member we created in setUpDatabase
        Optional<com.gymflow.model.WorkoutPlan> workoutPlan = workoutService.createWorkoutPlan(
            memberId, trainerId, "Test Workout", "Test", "Beginner"
        );
        assertTrue(workoutPlan.isPresent(), "Workout plan should be created");
        long workoutPlanId = workoutPlan.get().getId();

        // Create a class session
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        Optional<ClassSession> created = classScheduleService.createClassSession(
            trainerId, "Test Class", futureTime, 10
        );
        assertTrue(created.isPresent());
        long sessionId = created.get().getId();

        // Assign workout plan
        boolean assigned = classScheduleService.assignWorkoutPlanToClass(sessionId, workoutPlanId);

        assertTrue(assigned);
        Optional<ClassSession> updated = classScheduleService.getClassSessionById(sessionId);
        assertTrue(updated.isPresent());
        assertEquals(workoutPlanId, updated.get().getWorkoutPlanId());
    }
}

