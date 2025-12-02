package com.gymflow.util;

import com.gymflow.exception.FileOperationException;
import com.gymflow.exception.ValidationException;
import com.gymflow.model.AttendanceRecord;
import com.gymflow.model.WorkoutPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CsvUtil class.
 * Tests CSV import/export functionality for workout plans and attendance records.
 */
class CsvUtilTest {
    @TempDir
    Path tempDir;

    private List<WorkoutPlan> sampleWorkoutPlans;
    private List<AttendanceRecord> sampleAttendanceRecords;

    @BeforeEach
    void setUp() {
        // Create sample workout plans
        sampleWorkoutPlans = new ArrayList<>();
        sampleWorkoutPlans.add(new WorkoutPlan(1, 1, 2, "Morning Cardio", 
            "30-minute morning run", "Intermediate", "Full Body", "Cardio", 
            30, "Treadmill", null, null, null, LocalDateTime.now()));
        sampleWorkoutPlans.add(new WorkoutPlan(2, 1, 2, "Strength Training", 
            "Upper body workout", "Advanced", "Chest", "Strength Training", 
            45, "Dumbbells", 3, 12, 60, LocalDateTime.now()));

        // Create sample attendance records
        sampleAttendanceRecords = new ArrayList<>();
        sampleAttendanceRecords.add(new AttendanceRecord(1, 10, 1, true));
        sampleAttendanceRecords.add(new AttendanceRecord(2, 10, 2, false));
    }

    @Test
    void testExportWorkoutTemplates_Success() throws Exception {
        Path csvFile = tempDir.resolve("workout_plans.csv");
        
        CsvUtil.exportWorkoutTemplates(sampleWorkoutPlans, csvFile);
        
        assertTrue(Files.exists(csvFile), "CSV file should be created");
        String content = Files.readString(csvFile);
        assertTrue(content.contains("Title,Description,Difficulty"), "Should contain header");
        assertTrue(content.contains("Morning Cardio"), "Should contain workout plan data");
        assertTrue(content.contains("Muscle Group"), "Should contain new fields");
    }

    @Test
    void testExportWorkoutTemplates_NullPath_ThrowsException() {
        assertThrows(ValidationException.class, () -> {
            CsvUtil.exportWorkoutTemplates(sampleWorkoutPlans, null);
        });
    }

    @Test
    void testImportWorkoutTemplates_NewFormat_Success() throws Exception {
        // Create a CSV file with new format
        Path csvFile = tempDir.resolve("import_test.csv");
        String csvContent = """
            Title,Description,Difficulty,Member ID,Trainer ID,Muscle Group,Workout Type,Duration Minutes,Equipment Needed,Target Sets,Target Reps,Rest Seconds,Created At
            Test Workout,Test description,Intermediate,1,2,Legs,Strength Training,30,Barbell,3,10,60,2024-12-02 10:00:00
            """;
        Files.writeString(csvFile, csvContent);

        List<WorkoutPlan> imported = CsvUtil.importWorkoutTemplates(csvFile);

        assertEquals(1, imported.size());
        WorkoutPlan plan = imported.get(0);
        assertEquals("Test Workout", plan.getTitle());
        assertEquals("Legs", plan.getMuscleGroup());
        assertEquals("Strength Training", plan.getWorkoutType());
        assertEquals(30, plan.getDurationMinutes());
        assertEquals(3, plan.getTargetSets());
        assertEquals(10, plan.getTargetReps());
        assertEquals(60, plan.getRestSeconds());
    }

    @Test
    void testImportWorkoutTemplates_OldFormat_Success() throws Exception {
        // Create a CSV file with old format (backward compatibility)
        Path csvFile = tempDir.resolve("import_old.csv");
        String csvContent = """
            Title,Description,Difficulty,Member ID,Trainer ID,Created At
            Old Format Workout,Old description,Beginner,1,2,2024-12-02 10:00:00
            """;
        Files.writeString(csvFile, csvContent);

        List<WorkoutPlan> imported = CsvUtil.importWorkoutTemplates(csvFile);

        assertEquals(1, imported.size());
        WorkoutPlan plan = imported.get(0);
        assertEquals("Old Format Workout", plan.getTitle());
        // New fields should be null for old format
        assertNull(plan.getMuscleGroup());
        assertNull(plan.getWorkoutType());
    }

    @Test
    void testImportWorkoutTemplates_InvalidHeader_ThrowsException() throws Exception {
        Path csvFile = tempDir.resolve("invalid.csv");
        Files.writeString(csvFile, "Invalid,Header\nData,Row");

        assertThrows(ValidationException.class, () -> {
            CsvUtil.importWorkoutTemplates(csvFile);
        });
    }

    @Test
    void testImportWorkoutTemplates_EmptyFile_ThrowsException() throws Exception {
        Path csvFile = tempDir.resolve("empty.csv");
        Files.createFile(csvFile);

        assertThrows(ValidationException.class, () -> {
            CsvUtil.importWorkoutTemplates(csvFile);
        });
    }

    @Test
    void testImportWorkoutTemplates_MissingTitle_ThrowsException() throws Exception {
        Path csvFile = tempDir.resolve("missing_title.csv");
        String csvContent = """
            Title,Description,Difficulty,Member ID,Trainer ID,Muscle Group,Workout Type,Duration Minutes,Equipment Needed,Target Sets,Target Reps,Rest Seconds,Created At
            ,Test description,Intermediate,1,2,Legs,Strength Training,30,Barbell,3,10,60,2024-12-02 10:00:00
            """;
        Files.writeString(csvFile, csvContent);

        List<WorkoutPlan> imported = CsvUtil.importWorkoutTemplates(csvFile);
        // Should skip invalid rows
        assertEquals(0, imported.size());
    }

    @Test
    void testExportAttendanceReport_Success() throws Exception {
        Path csvFile = tempDir.resolve("attendance.csv");
        
        CsvUtil.exportAttendanceReport(sampleAttendanceRecords, csvFile);
        
        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertTrue(content.contains("Record ID,Session ID,Member ID,Attended"));
        assertTrue(content.contains("Yes"));
        assertTrue(content.contains("No"));
    }

    @Test
    void testExportAttendanceReport_WithNames_Success() throws Exception {
        Path csvFile = tempDir.resolve("attendance_with_names.csv");
        Map<Long, String> memberNameMap = new HashMap<>();
        memberNameMap.put(1L, "John Doe");
        memberNameMap.put(2L, "Jane Smith");
        Map<Long, String> classNameMap = new HashMap<>();
        classNameMap.put(10L, "Morning Yoga");

        CsvUtil.exportAttendanceReport(sampleAttendanceRecords, csvFile, memberNameMap, classNameMap);

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertTrue(content.contains("Member Name"));
        assertTrue(content.contains("Class Name"));
        assertTrue(content.contains("John Doe"));
        assertTrue(content.contains("Morning Yoga"));
    }

    @Test
    void testValidateFile_NonExistent_ThrowsException() {
        Path nonExistent = tempDir.resolve("nonexistent.csv");
        
        assertThrows(FileOperationException.class, () -> {
            CsvUtil.validateFile(nonExistent);
        });
    }

    @Test
    void testValidateFile_NotCsv_ThrowsException() throws Exception {
        Path txtFile = tempDir.resolve("test.txt");
        Files.createFile(txtFile);

        assertThrows(ValidationException.class, () -> {
            CsvUtil.validateFile(txtFile);
        });
    }

    @Test
    void testValidateFile_TooLarge_ThrowsException() throws Exception {
        Path largeFile = tempDir.resolve("large.csv");
        // Create a file larger than 10MB
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
        Files.write(largeFile, largeContent);

        assertThrows(ValidationException.class, () -> {
            CsvUtil.validateFile(largeFile);
        });
    }

    @Test
    void testValidateFile_EmptyFile_ThrowsException() throws Exception {
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.createFile(emptyFile);

        assertThrows(ValidationException.class, () -> {
            CsvUtil.validateFile(emptyFile);
        });
    }

    @Test
    void testExportImportRoundTrip_Success() throws Exception {
        Path csvFile = tempDir.resolve("roundtrip.csv");
        
        // Export
        CsvUtil.exportWorkoutTemplates(sampleWorkoutPlans, csvFile);
        
        // Import
        List<WorkoutPlan> imported = CsvUtil.importWorkoutTemplates(csvFile);
        
        assertEquals(sampleWorkoutPlans.size(), imported.size());
        assertEquals(sampleWorkoutPlans.get(0).getTitle(), imported.get(0).getTitle());
        assertEquals(sampleWorkoutPlans.get(0).getMuscleGroup(), imported.get(0).getMuscleGroup());
    }

    @Test
    void testCsvFieldEscaping_CommasInFields() throws Exception {
        WorkoutPlan planWithComma = new WorkoutPlan(1, 1, 2, "Plan, with comma",
            "Description, with comma", "Intermediate", "Chest", "Strength Training",
            30, "Dumbbells, Barbell", 3, 12, 60, LocalDateTime.now());
        List<WorkoutPlan> plans = List.of(planWithComma);
        
        Path csvFile = tempDir.resolve("escaped.csv");
        CsvUtil.exportWorkoutTemplates(plans, csvFile);
        
        List<WorkoutPlan> imported = CsvUtil.importWorkoutTemplates(csvFile);
        assertEquals(1, imported.size());
        assertEquals("Plan, with comma", imported.get(0).getTitle());
        assertEquals("Description, with comma", imported.get(0).getDescription());
    }
}

