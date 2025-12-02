package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.dao.WorkoutPlanDao;
import com.gymflow.dao.WorkoutPlanDaoImpl;
import com.gymflow.exception.DataAccessException;
import com.gymflow.model.WorkoutPlan;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for WorkoutServiceImpl using H2 in-memory database.
 */
class WorkoutServiceImplTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection;
    private WorkoutServiceImpl workoutService;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:testdb_workout;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dbConnection = DatabaseConnection.getInstance();
        testConnection = dbConnection.getConnection();
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS workout_plans (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    member_id INT NOT NULL,
                    trainer_id INT NOT NULL,
                    title VARCHAR(100) NOT NULL,
                    description TEXT,
                    difficulty VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            stmt.execute("DELETE FROM workout_plans");
            stmt.execute("""
                INSERT INTO workout_plans (id, member_id, trainer_id, title, description, difficulty)
                VALUES (1, 1, 2, 'Test Plan', 'A test workout plan', 'Beginner')
            """);
        }
    }

    @BeforeEach
    void setUp() {
        workoutService = new WorkoutServiceImpl();
    }

    @Test
    void testGetWorkoutPlanById_Existing() throws DataAccessException {
        Optional<WorkoutPlan> result = workoutService.getWorkoutPlanById(1);
        assertTrue(result.isPresent());
        assertEquals("Test Plan", result.get().getTitle());
    }

    @Test
    void testGetWorkoutPlanById_NonExistent() throws DataAccessException {
        Optional<WorkoutPlan> result = workoutService.getWorkoutPlanById(999);
        assertFalse(result.isPresent());
    }

    @Test
    void testCreateWorkoutPlan_Valid() throws DataAccessException {
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
    void testDeleteWorkoutPlan_Existing() throws DataAccessException {
        boolean deleted = workoutService.deleteWorkoutPlan(1);
        assertTrue(deleted);
    }

    @Test
    void testDeleteWorkoutPlan_NonExistent() {
        assertThrows(DataAccessException.class, () ->
            workoutService.deleteWorkoutPlan(999)
        );
    }
}
