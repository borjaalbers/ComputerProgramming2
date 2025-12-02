package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.AttendanceRecord;
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
 * Integration test for AttendanceDao using H2 in-memory database.
 */
class AttendanceDaoTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection;
    private AttendanceDao attendanceDao;
    private ClassSessionDao classSessionDao;
    private long memberId;
    private long trainerId;
    private long sessionId;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:attendancedao_testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
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
        attendanceDao = new AttendanceDaoImpl();
        classSessionDao = new ClassSessionDaoImpl();
        memberId = 1;
        trainerId = 2;

        // Create a test class session
        com.gymflow.model.ClassSession session = new com.gymflow.model.ClassSession(
            0, trainerId, "Test Class", LocalDateTime.now().plusDays(1), 10, null
        );
        Optional<com.gymflow.model.ClassSession> created = classSessionDao.create(session);
        assertTrue(created.isPresent());
        sessionId = created.get().getId();
    }

    @Test
    void testMarkAttendance_NewRecord_Success() {
        Optional<AttendanceRecord> result = attendanceDao.markAttendance(sessionId, memberId, true);

        assertTrue(result.isPresent());
        AttendanceRecord record = result.get();
        assertTrue(record.isAttended());
        assertEquals(sessionId, record.getSessionId());
        assertEquals(memberId, record.getMemberId());
    }

    @Test
    void testMarkAttendance_UpdateExisting_Success() {
        // First mark as not attended
        attendanceDao.markAttendance(sessionId, memberId, false);
        
        // Then update to attended
        Optional<AttendanceRecord> result = attendanceDao.markAttendance(sessionId, memberId, true);

        assertTrue(result.isPresent());
        assertTrue(result.get().isAttended(), "Should be updated to attended");
    }

    @Test
    void testFindBySessionId_Success() {
        attendanceDao.markAttendance(sessionId, memberId, true);

        List<AttendanceRecord> records = attendanceDao.findBySessionId(sessionId);

        assertFalse(records.isEmpty());
        assertTrue(records.stream().anyMatch(r -> r.getMemberId() == memberId));
    }

    @Test
    void testFindByMemberId_Success() {
        attendanceDao.markAttendance(sessionId, memberId, true);

        List<AttendanceRecord> records = attendanceDao.findByMemberId(memberId);

        assertFalse(records.isEmpty());
        assertTrue(records.stream().anyMatch(r -> r.getSessionId() == sessionId));
    }

    @Test
    void testFindById_Success() {
        Optional<AttendanceRecord> created = attendanceDao.markAttendance(sessionId, memberId, true);
        assertTrue(created.isPresent());
        long recordId = created.get().getId();

        Optional<AttendanceRecord> found = attendanceDao.findById(recordId);

        assertTrue(found.isPresent());
        assertEquals(sessionId, found.get().getSessionId());
        assertEquals(memberId, found.get().getMemberId());
    }

    @Test
    void testDelete_Success() {
        attendanceDao.markAttendance(sessionId, memberId, true);

        boolean deleted = attendanceDao.delete(sessionId, memberId);

        assertTrue(deleted);
        Optional<AttendanceRecord> found = attendanceDao.findBySessionAndMember(sessionId, memberId);
        assertFalse(found.isPresent(), "Record should be deleted");
    }

    @Test
    void testFindBySessionAndMember_Success() {
        attendanceDao.markAttendance(sessionId, memberId, true);

        Optional<AttendanceRecord> found = attendanceDao.findBySessionAndMember(sessionId, memberId);

        assertTrue(found.isPresent());
        assertEquals(sessionId, found.get().getSessionId());
        assertEquals(memberId, found.get().getMemberId());
    }

    @Test
    void testFindAll_Success() {
        attendanceDao.markAttendance(sessionId, memberId, true);

        List<AttendanceRecord> all = attendanceDao.findAll();

        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(r -> r.getSessionId() == sessionId && r.getMemberId() == memberId));
    }
}

