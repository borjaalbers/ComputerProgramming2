package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.dao.WorkoutPlanDao;
import com.gymflow.dao.WorkoutPlanDaoImpl;
import com.gymflow.exception.DataAccessException;
import com.gymflow.model.WorkoutPlan;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for WorkoutPlanServiceImpl using H2 in-memory database.
 */
class WorkoutPlanServiceImplTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection;
    private WorkoutPlanServiceImpl workoutPlanService;
    private static Path tempCsv;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        DatabaseConnection.resetInstance();
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:testdb_wps;DB_CLOSE_DELAY=-1;MODE=MySQL");
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
                VALUES (1, 1, 2, 'Export Plan', 'Export test', 'Beginner')
            """);
        }
        tempCsv = Files.createTempFile("workout_export", ".csv");
    }

    @BeforeEach
    void setUp() {
        WorkoutPlanDao dao = new WorkoutPlanDaoImpl();
        workoutPlanService = new WorkoutPlanServiceImpl(dao);
    }

    @Test
    void testExportWorkoutTemplates() throws Exception {
        assertDoesNotThrow(() -> workoutPlanService.exportWorkoutTemplates(tempCsv));
        assertTrue(Files.size(tempCsv) > 0);
    }

    @Test
    void testImportWorkoutTemplates() throws Exception {
        // Export first, then import
        workoutPlanService.exportWorkoutTemplates(tempCsv);
        List<WorkoutPlan> imported = workoutPlanService.importWorkoutTemplates(tempCsv);
        assertFalse(imported.isEmpty());
        assertEquals("Export Plan", imported.get(0).getTitle());
    }
}
