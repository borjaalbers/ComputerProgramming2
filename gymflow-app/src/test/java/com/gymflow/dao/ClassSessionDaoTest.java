package com.gymflow.dao;

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
 * Integration test for ClassSessionDao using H2 in-memory database.
 */
class ClassSessionDaoTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection;
    private ClassSessionDao classSessionDao;
    private long trainerId;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:classdao_testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
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
        classSessionDao = new ClassSessionDaoImpl();
        trainerId = 2;
    }

    @Test
    void testCreate_WithWorkoutPlan_Success() throws Exception {
        // Create a workout plan first
        WorkoutPlanDao workoutPlanDao = new WorkoutPlanDaoImpl();
        long memberId = 1; // Use the member we created in setUpDatabase
        com.gymflow.model.WorkoutPlan workoutPlan = new com.gymflow.model.WorkoutPlan(
            0, memberId, trainerId, "Test Workout", "Test", "Beginner", null, null, null, null, null, null, null, LocalDateTime.now()
        );
        Optional<com.gymflow.model.WorkoutPlan> createdPlan = workoutPlanDao.create(workoutPlan);
        assertTrue(createdPlan.isPresent(), "Workout plan should be created");
        Long workoutPlanId = createdPlan.get().getId();

        ClassSession session = new ClassSession(0, trainerId, "Test Class",
            LocalDateTime.now().plusDays(1), 20, workoutPlanId);

        Optional<ClassSession> result = classSessionDao.create(session);

        assertTrue(result.isPresent());
        ClassSession created = result.get();
        assertTrue(created.getId() > 0);
        assertEquals("Test Class", created.getTitle());
        assertEquals(workoutPlanId, created.getWorkoutPlanId());
    }

    @Test
    void testCreate_WithoutWorkoutPlan_Success() {
        ClassSession session = new ClassSession(0, trainerId, "Simple Class",
            LocalDateTime.now().plusDays(1), 15, null);

        Optional<ClassSession> result = classSessionDao.create(session);

        assertTrue(result.isPresent());
        assertEquals("Simple Class", result.get().getTitle());
        assertNull(result.get().getWorkoutPlanId());
    }

    @Test
    void testFindById_Success() {
        ClassSession session = new ClassSession(0, trainerId, "Find Me",
            LocalDateTime.now().plusDays(1), 10, null);
        Optional<ClassSession> created = classSessionDao.create(session);
        assertTrue(created.isPresent());
        long sessionId = created.get().getId();

        Optional<ClassSession> found = classSessionDao.findById(sessionId);

        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getTitle());
    }

    @Test
    void testFindByTrainerId_Success() {
        ClassSession session1 = new ClassSession(0, trainerId, "Class 1",
            LocalDateTime.now().plusDays(1), 10, null);
        ClassSession session2 = new ClassSession(0, trainerId, "Class 2",
            LocalDateTime.now().plusDays(2), 15, null);
        classSessionDao.create(session1);
        classSessionDao.create(session2);

        List<ClassSession> sessions = classSessionDao.findByTrainerId(trainerId);

        assertTrue(sessions.size() >= 2);
        assertTrue(sessions.stream().anyMatch(s -> s.getTitle().equals("Class 1")));
        assertTrue(sessions.stream().anyMatch(s -> s.getTitle().equals("Class 2")));
    }

    @Test
    void testFindUpcoming_Success() {
        ClassSession past = new ClassSession(0, trainerId, "Past Class",
            LocalDateTime.now().minusDays(1), 10, null);
        ClassSession future = new ClassSession(0, trainerId, "Future Class",
            LocalDateTime.now().plusDays(1), 10, null);
        classSessionDao.create(past);
        classSessionDao.create(future);

        List<ClassSession> upcoming = classSessionDao.findUpcoming();

        assertFalse(upcoming.isEmpty());
        assertTrue(upcoming.stream().anyMatch(s -> s.getTitle().equals("Future Class")));
        assertFalse(upcoming.stream().anyMatch(s -> s.getTitle().equals("Past Class")));
    }

    @Test
    void testUpdate_Success() {
        ClassSession session = new ClassSession(0, trainerId, "Original",
            LocalDateTime.now().plusDays(1), 10, null);
        Optional<ClassSession> created = classSessionDao.create(session);
        assertTrue(created.isPresent());
        ClassSession toUpdate = created.get();
        toUpdate.setTitle("Updated");
        toUpdate.setCapacity(25);

        boolean updated = classSessionDao.update(toUpdate);

        assertTrue(updated);
        Optional<ClassSession> found = classSessionDao.findById(toUpdate.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getTitle());
        assertEquals(25, found.get().getCapacity());
    }

    @Test
    void testDelete_Success() {
        ClassSession session = new ClassSession(0, trainerId, "To Delete",
            LocalDateTime.now().plusDays(1), 10, null);
        Optional<ClassSession> created = classSessionDao.create(session);
        assertTrue(created.isPresent());
        long sessionId = created.get().getId();

        boolean deleted = classSessionDao.delete(sessionId);

        assertTrue(deleted);
        Optional<ClassSession> found = classSessionDao.findById(sessionId);
        assertFalse(found.isPresent(), "Session should be deleted");
    }
}

