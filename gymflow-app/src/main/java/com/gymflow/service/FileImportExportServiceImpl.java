package com.gymflow.service;

import com.gymflow.model.AttendanceRecord;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.util.CsvUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Implementation of FileImportExportService for file I/O operations.
 */
public class FileImportExportServiceImpl implements FileImportExportService {

    @Override
    public boolean exportWorkoutTemplates(List<WorkoutPlan> workoutPlans, String filePath) throws IOException {
        if (workoutPlans == null) {
            throw new IllegalArgumentException("Workout plans list cannot be null");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        CsvUtil.exportWorkoutTemplates(workoutPlans, path);
        return true;
    }

    @Override
    public List<WorkoutPlan> importWorkoutTemplates(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        CsvUtil.validateFile(path);
        return CsvUtil.importWorkoutTemplates(path);
    }

    @Override
    public boolean exportAttendanceReport(List<AttendanceRecord> attendanceRecords, String filePath) throws IOException {
        if (attendanceRecords == null) {
            throw new IllegalArgumentException("Attendance records list cannot be null");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        CsvUtil.exportAttendanceReport(attendanceRecords, path);
        return true;
    }

    @Override
    public void validateFile(String filePath) throws FileNotFoundException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        CsvUtil.validateFile(path);
    }
}

