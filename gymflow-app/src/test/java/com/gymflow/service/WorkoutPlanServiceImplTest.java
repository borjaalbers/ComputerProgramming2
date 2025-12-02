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
            // 1. FORCE DROP the old table so we don't get stuck with the old schema
            stmt.execute("DROP TABLE IF EXISTS workout_plans");

            // 2. Create the table fresh with ALL columns
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

            // 3. Insert the test data
            stmt.execute("""
                INSERT INTO workout_plans (
                    id, member_id, trainer_id, title, description, difficulty, 
                    muscle_group, workout_type, duration_minutes, target_sets, target_reps
                )
                VALUES (
                    1, 1, 2, 'Export Plan', 'Export test', 'Beginner', 
                    'Full Body', 'Strength', 60, 3, 12
                )
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
        // 1. Manually write a valid CSV string with the EXACT required headers and matching data
        String dummyCsvContent = """
            Title,Description,Difficulty,MuscleGroup,WorkoutType,DurationMinutes,EquipmentNeeded,TargetSets,TargetReps,RestSeconds,MemberId,TrainerId,CreatedAt
            Imported Plan,Test Description,Beginner,Chest,Strength,60,None,3,12,60,1,2,2023-01-01T12:00:00
            """;

        Files.writeString(tempCsv, dummyCsvContent);

        // 2. Run the Import Logic
        List<WorkoutPlan> imported = workoutPlanService.importWorkoutTemplates(tempCsv);

        // 3. Assertions
        assertFalse(imported.isEmpty(), "Imported list should not be empty");
        assertEquals("Imported Plan", imported.get(0).getTitle());
        assertEquals("Chest", imported.get(0).getMuscleGroup());
    }
}
