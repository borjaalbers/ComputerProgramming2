package com.gymflow.util;

import com.gymflow.exception.FileOperationException;
import com.gymflow.exception.ValidationException;
import com.gymflow.model.AttendanceRecord;
import com.gymflow.model.WorkoutPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvUtilTest {
    private Path tempFile;

    @AfterEach
    void cleanup() throws Exception {
        if (tempFile != null && Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    @Test
    void exportAndImportWorkoutTemplates_Success() throws Exception {
        tempFile = Files.createTempFile("workout", ".csv");
        WorkoutPlan plan = new WorkoutPlan(0, 1, 2, "Title", "Desc", "Easy", "Legs", "Strength", 30, "Dumbbell", 3, 10, 60, LocalDateTime.now());
        CsvUtil.exportWorkoutTemplates(List.of(plan), tempFile);
        assertTrue(Files.size(tempFile) > 0);
        List<WorkoutPlan> imported = CsvUtil.importWorkoutTemplates(tempFile);
        assertEquals(1, imported.size());
        assertEquals("Title", imported.get(0).getTitle());
    }

    @Test
    void exportWorkoutTemplates_NullPath_Throws() {
        assertThrows(FileOperationException.class, () -> CsvUtil.exportWorkoutTemplates(List.of(), null));
    }

    @Test
    void importWorkoutTemplates_InvalidHeader_Throws() throws Exception {
        tempFile = Files.createTempFile("badheader", ".csv");
        Files.writeString(tempFile, "Bad,Header\n1,2,3");
        assertThrows(ValidationException.class, () -> CsvUtil.importWorkoutTemplates(tempFile));
    }

    @Test
    void exportAndImportAttendanceReport_Success() throws Exception {
        tempFile = Files.createTempFile("attendance", ".csv");
        AttendanceRecord rec = new AttendanceRecord(0, 5, 10, true);
        CsvUtil.exportAttendanceReport(List.of(rec), tempFile);
        assertTrue(Files.size(tempFile) > 0);
        List<AttendanceRecord> imported = CsvUtil.importAttendanceReport(tempFile);
        assertEquals(1, imported.size());
        assertEquals(10, imported.get(0).getMemberId());
        assertTrue(imported.get(0).isAttended());
    }

    @Test
    void importAttendanceReport_InvalidHeader_Throws() throws Exception {
        tempFile = Files.createTempFile("badheader2", ".csv");
        Files.writeString(tempFile, "Bad,Header\n1,2,Completed");
        assertThrows(ValidationException.class, () -> CsvUtil.importAttendanceReport(tempFile));
    }

    @Test
    void importWorkoutTemplates_EmptyFile_Throws() throws Exception {
        tempFile = Files.createTempFile("empty", ".csv");
        Files.writeString(tempFile, "");
        assertThrows(ValidationException.class, () -> CsvUtil.importWorkoutTemplates(tempFile));
    }
}
