package com.gymflow.service;

import com.gymflow.exception.FileOperationException;
import com.gymflow.model.AttendanceRecord;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.util.CsvUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service interface for file import/export operations.
 */
public interface FileImportExportService {
    /**
     * Exports workout plans to a CSV file.
     *
     * @param workoutPlans the list of workout plans to export
     * @param filePath the path where the CSV file will be created
     * @return true if export was successful, false otherwise
     * @throws FileOperationException if an I/O error occurs
     */
    boolean exportWorkoutTemplates(List<WorkoutPlan> workoutPlans, String filePath) throws FileOperationException;

    /**
     * Imports workout plans from a CSV file.
     *
     * @param filePath the path to the CSV file to import
     * @return list of imported workout plans
     * @throws FileOperationException if an I/O error occurs or file format is invalid
     */
    List<WorkoutPlan> importWorkoutTemplates(String filePath) throws FileOperationException;

    /**
     * Exports attendance records to a CSV file.
     *
     * @param attendanceRecords the list of attendance records to export
     * @param filePath the path where the CSV file will be created
     * @return true if export was successful, false otherwise
     * @throws FileOperationException if an I/O error occurs
     */
    boolean exportAttendanceReport(List<AttendanceRecord> attendanceRecords, String filePath) throws FileOperationException;

    /**
     * Exports attendance records to a CSV file with member and class names.
     *
     * @param attendanceRecords the list of attendance records to export
     * @param filePath the path where the CSV file will be created
     * @param memberNameMap optional map of member ID to member name (can be null)
     * @param classNameMap optional map of session ID to class name (can be null)
     * @return true if export was successful, false otherwise
     * @throws FileOperationException if an I/O error occurs
     */
    boolean exportAttendanceReport(List<AttendanceRecord> attendanceRecords, String filePath,
                                   java.util.Map<Long, String> memberNameMap,
                                   java.util.Map<Long, String> classNameMap) throws FileOperationException;

    /**
     * Validates a file before import.
     *
     * @param filePath the path to validate
     * @throws FileOperationException if the file doesn't exist or is invalid
     */
    void validateFile(String filePath) throws FileOperationException;
}

