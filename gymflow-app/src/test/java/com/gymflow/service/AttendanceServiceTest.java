package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.AttendanceRecord;
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
 * Integration test for AttendanceService using H2 in-memory database.
 */
class AttendanceServiceTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection;
    private AttendanceService attendanceService;
    private ClassScheduleService classScheduleService;
    private long memberId;
    private long trainerId;
    private long sessionId;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:attendance_testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
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
                CREATE TABLE IF NOT EXISTS class_sessions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    trainer_id INT,
                    title VARCHAR(150) NOT NULL,
                    schedule_timestamp TIMESTAMP NOT NULL,
                    capacity INT DEFAULT 10,
                    workout_plan_id INT,
                    FOREIGN KEY (trainer_id) REFERENCES users(id)
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
        attendanceService = new AttendanceServiceImpl();
        classScheduleService = new ClassScheduleServiceImpl();
        memberId = 1;
        trainerId = 2;

        // Create a test class session
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        Optional<ClassSession> session = classScheduleService.createClassSession(
            trainerId, "Test Class", futureTime, 10
        );
        assertTrue(session.isPresent());
        sessionId = session.get().getId();
    }

    @Test
    void testMarkAttendance_Attended_Success() {
        Optional<AttendanceRecord> result = attendanceService.markAttendance(sessionId, memberId, true);

        assertTrue(result.isPresent(), "Attendance should be marked");
        AttendanceRecord record = result.get();
        assertTrue(record.isAttended(), "Member should be marked as attended");
        assertEquals(sessionId, record.getSessionId());
        assertEquals(memberId, record.getMemberId());
    }

    @Test
    void testMarkAttendance_NotAttended_Success() {
        Optional<AttendanceRecord> result = attendanceService.markAttendance(sessionId, memberId, false);

        assertTrue(result.isPresent());
        assertFalse(result.get().isAttended(), "Member should be marked as not attended");
    }

    @Test
    void testGetAttendanceForSession_Success() {
        attendanceService.markAttendance(sessionId, memberId, true);

        List<AttendanceRecord> records = attendanceService.getAttendanceForSession(sessionId);

        assertFalse(records.isEmpty());
        assertTrue(records.stream().anyMatch(r -> r.getMemberId() == memberId));
    }

    @Test
    void testGetAttendanceForMember_Success() {
        attendanceService.markAttendance(sessionId, memberId, true);

        List<AttendanceRecord> records = attendanceService.getAttendanceForMember(memberId);

        assertFalse(records.isEmpty());
        assertTrue(records.stream().anyMatch(r -> r.getSessionId() == sessionId));
    }

    @Test
    void testGetAttendanceCount_Success() {
        attendanceService.markAttendance(sessionId, memberId, true);

        int count = attendanceService.getAttendanceCount(sessionId);

        assertEquals(1, count, "Should count one attended member");
    }

    @Test
    void testRegisterForClass_Success() {
        Optional<AttendanceRecord> result = attendanceService.registerForClass(sessionId, memberId);

        assertTrue(result.isPresent());
        assertFalse(result.get().isAttended(), "Registered members should not be marked as attended initially");
    }

    @Test
    void testUnregisterFromClass_Success() {
        attendanceService.registerForClass(sessionId, memberId);

        boolean unregistered = attendanceService.unregisterFromClass(sessionId, memberId);

        assertTrue(unregistered);
        assertFalse(attendanceService.isRegisteredForClass(sessionId, memberId));
    }

    @Test
    void testIsRegisteredForClass_Registered_ReturnsTrue() {
        attendanceService.registerForClass(sessionId, memberId);

        boolean registered = attendanceService.isRegisteredForClass(sessionId, memberId);

        assertTrue(registered);
    }

    @Test
    void testIsRegisteredForClass_NotRegistered_ReturnsFalse() {
        boolean registered = attendanceService.isRegisteredForClass(sessionId, memberId);

        assertFalse(registered);
    }

    @Test
    void testGetRegisteredCount_Success() {
        attendanceService.registerForClass(sessionId, memberId);

        int count = attendanceService.getRegisteredCount(sessionId);

        assertEquals(1, count);
    }
}

