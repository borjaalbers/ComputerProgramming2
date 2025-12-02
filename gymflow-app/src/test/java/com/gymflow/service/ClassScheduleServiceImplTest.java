package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.dao.ClassSessionDao;
import com.gymflow.dao.ClassSessionDaoImpl;
import com.gymflow.exception.DataAccessException;
import com.gymflow.model.ClassSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ClassScheduleServiceImpl using H2 in-memory database.
 */
class ClassScheduleServiceImplTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection;
    private ClassScheduleServiceImpl classScheduleService;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:testdb_class;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dbConnection = DatabaseConnection.getInstance();
        testConnection = dbConnection.getConnection();
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS class_sessions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    trainer_id INT NOT NULL,
                    title VARCHAR(100) NOT NULL,
                    schedule_timestamp TIMESTAMP NOT NULL,
                    capacity INT NOT NULL,
                    workout_plan_id INT
                )
            """);
            stmt.execute("DELETE FROM class_sessions");
            stmt.execute(String.format("""
                INSERT INTO class_sessions (id, trainer_id, title, schedule_timestamp, capacity)
                VALUES (1, 2, 'Morning Yoga', '%s', 10)
            """, LocalDateTime.now().plusDays(1)));
        }
    }

    @BeforeEach
    void setUp() {
        ClassSessionDao dao = new ClassSessionDaoImpl();
        classScheduleService = new ClassScheduleServiceImpl();
    }

    @Test
    void testGetClassSessionById_Existing() throws DataAccessException {
        Optional<ClassSession> result = classScheduleService.getClassSessionById(1);
        assertTrue(result.isPresent());
        assertEquals("Morning Yoga", result.get().getTitle());
    }

    @Test
    void testGetClassSessionById_NonExistent() throws DataAccessException {
        Optional<ClassSession> result = classScheduleService.getClassSessionById(999);
        assertFalse(result.isPresent());
    }

    @Test
    void testCreateClassSession_Valid() throws DataAccessException {
        Optional<ClassSession> created = classScheduleService.createClassSession(2, "Evening Pilates", LocalDateTime.now().plusDays(2), 15);
        assertTrue(created.isPresent());
        assertEquals("Evening Pilates", created.get().getTitle());
    }

    @Test
    void testCreateClassSession_InvalidTitle() {
        assertThrows(IllegalArgumentException.class, () ->
            classScheduleService.createClassSession(2, "", LocalDateTime.now().plusDays(2), 15)
        );
    }

    @Test
    void testDeleteClassSession_Existing() throws DataAccessException {
        boolean deleted = classScheduleService.deleteClassSession(1);
        assertTrue(deleted);
    }

    @Test
    void testDeleteClassSession_NonExistent() {
        assertThrows(DataAccessException.class, () ->
            classScheduleService.deleteClassSession(999)
        );
    }
}
