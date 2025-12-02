package com.gymflow.service;

import com.gymflow.exception.FileOperationException;
import com.gymflow.model.AttendanceRecord;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.util.CsvUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Implementation of FileImportExportService for file I/O operations.
 */
public class FileImportExportServiceImpl implements FileImportExportService {

    @Override
    public boolean exportWorkoutTemplates(List<WorkoutPlan> workoutPlans, String filePath) throws FileOperationException {
        if (workoutPlans == null) {
            throw new com.gymflow.exception.ValidationException("Workout plans list cannot be null");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new com.gymflow.exception.ValidationException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        CsvUtil.exportWorkoutTemplates(workoutPlans, path);
        return true;
    }

    @Override
    public List<WorkoutPlan> importWorkoutTemplates(String filePath) throws FileOperationException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new com.gymflow.exception.ValidationException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        CsvUtil.validateFile(path);
        return CsvUtil.importWorkoutTemplates(path);
    }

    @Override
    public boolean exportAttendanceReport(List<AttendanceRecord> attendanceRecords, String filePath) throws FileOperationException {
        return exportAttendanceReport(attendanceRecords, filePath, null, null);
    }

    @Override
    public boolean exportAttendanceReport(List<AttendanceRecord> attendanceRecords, String filePath,
                                        java.util.Map<Long, String> memberNameMap,
                                        java.util.Map<Long, String> classNameMap) throws FileOperationException {
        if (attendanceRecords == null) {
            throw new com.gymflow.exception.ValidationException("Attendance records list cannot be null");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new com.gymflow.exception.ValidationException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        CsvUtil.exportAttendanceReport(attendanceRecords, path, memberNameMap, classNameMap);
        return true;
    }

    @Override
    public void validateFile(String filePath) throws FileOperationException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new com.gymflow.exception.ValidationException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        CsvUtil.validateFile(path);
    }
}

