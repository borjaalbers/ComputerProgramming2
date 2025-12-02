package com.gymflow.util;

import com.gymflow.model.AttendanceRecord;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.exception.FileOperationException;
import com.gymflow.exception.ValidationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for CSV import/export operations.
 * Handles workout plan templates and attendance reports.
 */
public final class CsvUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_FILE_SIZE_MB = 10;
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

    private CsvUtil() { /* prevent instantiation */ }

    // ==========================
    // Export Workout Plans
    // ==========================
    /**
     * Exports a list of workout plans to a CSV file.
     *
     * @param workoutPlans the list of workout plans to export
     * @param targetPath the path where the CSV file will be created
     * @throws FileOperationException if an I/O error occurs
     */
    public static void exportWorkoutTemplates(List<WorkoutPlan> workoutPlans, Path targetPath) throws FileOperationException {
        if (targetPath == null) throw new FileOperationException("Target path cannot be null");

        try {
            Path parentDir = targetPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) Files.createDirectories(parentDir);

            try (BufferedWriter writer = Files.newBufferedWriter(targetPath)) {
                // CSV Header
                writer.write("Title,Description,Difficulty,MuscleGroup,WorkoutType,DurationMinutes,EquipmentNeeded,TargetSets,TargetReps,RestSeconds,MemberId,TrainerId,CreatedAt");
                writer.newLine();

                // CSV Rows
                for (WorkoutPlan plan : workoutPlans) {
                    writer.write(
                            escapeCsvField(plan.getTitle()) + "," +
                                    escapeCsvField(plan.getDescription()) + "," +
                                    escapeCsvField(plan.getDifficulty()) + "," +
                                    escapeCsvField(plan.getMuscleGroup()) + "," +
                                    escapeCsvField(plan.getWorkoutType()) + "," +
                                    plan.getDurationMinutes() + "," +
                                    escapeCsvField(plan.getEquipmentNeeded()) + "," +
                                    plan.getTargetSets() + "," +
                                    plan.getTargetReps() + "," +
                                    plan.getRestSeconds() + "," +
                                    plan.getMemberId() + "," +
                                    plan.getTrainerId() + "," +
                                    plan.getCreatedAt().format(DATE_TIME_FORMATTER)
                    );
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new FileOperationException("Error exporting workout templates", e);
        }
    }

    // ==========================
    // Import Workout Plans
    // ==========================
    /**
     * Imports workout plans from a CSV file.
     *
     * @param sourcePath the path to the CSV file to import
     * @return list of imported WorkoutPlan objects
     * @throws FileOperationException if file I/O fails
     * @throws ValidationException if CSV content is invalid
     */
    public static List<WorkoutPlan> importWorkoutTemplates(Path sourcePath) throws FileOperationException, ValidationException {
        validateFile(sourcePath);

        List<WorkoutPlan> workoutPlans = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(sourcePath)) {
            String headerLine = reader.readLine();
            if (headerLine == null) throw new ValidationException("CSV file is empty");

            String expectedHeader = "Title,Description,Difficulty,MuscleGroup,WorkoutType,DurationMinutes,EquipmentNeeded,TargetSets,TargetReps,RestSeconds,MemberId,TrainerId,CreatedAt";
            if (!headerLine.trim().equalsIgnoreCase(expectedHeader)) {
                throw new ValidationException("Invalid CSV header. Expected: " + expectedHeader);
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    WorkoutPlan plan = parseWorkoutPlanLine(line, lineNumber);
                    workoutPlans.add(plan);
                } catch (ValidationException e) {
                    System.err.println("Skipping line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new FileOperationException("Error importing workout templates", e);
        }

        return workoutPlans;
    }

    private static WorkoutPlan parseWorkoutPlanLine(String line, int lineNumber) throws ValidationException {
        String[] fields = parseCsvLine(line);
        if (fields.length < 13) throw new ValidationException("Line " + lineNumber + " has insufficient fields (expected 13, got " + fields.length + ")");

        try {
            String title = unescapeCsvField(fields[0]);
            String description = unescapeCsvField(fields[1]);
            String difficulty = unescapeCsvField(fields[2]);
            String muscleGroup = unescapeCsvField(fields[3]);
            String workoutType = unescapeCsvField(fields[4]);
            int durationMinutes = Integer.parseInt(fields[5].trim());
            String equipment = unescapeCsvField(fields[6]);
            int sets = Integer.parseInt(fields[7].trim());
            int reps = Integer.parseInt(fields[8].trim());
            int rest = Integer.parseInt(fields[9].trim());
            long memberId = Long.parseLong(fields[10].trim());
            long trainerId = Long.parseLong(fields[11].trim());
            LocalDateTime createdAt = LocalDateTime.now();

            if (fields[12] != null && !fields[12].trim().isEmpty()) {
                try {
                    createdAt = LocalDateTime.parse(fields[12].trim(), DATE_TIME_FORMATTER);
                } catch (DateTimeParseException e) {
                    System.err.println("Warning: invalid date on line " + lineNumber + ", using current time");
                }
            }

            if (title == null || title.isEmpty()) throw new ValidationException("Title is required");
            if (memberId <= 0) throw new ValidationException("Member ID must be >0");
            if (trainerId <= 0) throw new ValidationException("Trainer ID must be >0");

            return new WorkoutPlan(0, memberId, trainerId, title, description, difficulty, muscleGroup, workoutType, durationMinutes, equipment, sets, reps, rest, createdAt);

        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid number format: " + e.getMessage());
        }
    }

    // ==========================
    // Export Attendance Report
    // ==========================
    /**
     * Exports a list of attendance records to a CSV file.
     *
     * @param attendanceRecords the list of attendance records to export
     * @param targetPath the path to write the CSV
     * @throws FileOperationException if file I/O fails
     */
    public static void exportAttendanceReport(List<AttendanceRecord> attendanceRecords, Path targetPath) throws FileOperationException {
        if (targetPath == null) throw new FileOperationException("Target path cannot be null");

        try {
            Path parentDir = targetPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) Files.createDirectories(parentDir);

            try (BufferedWriter writer = Files.newBufferedWriter(targetPath)) {
                writer.write("MemberId,SessionId,Attended");
                writer.newLine();

                for (AttendanceRecord record : attendanceRecords) {
                    writer.write(String.format("%d,%d,%s",
                            record.getMemberId(),
                            record.getSessionId(),
                            record.isAttended() ? "Completed" : "Pending"
                    ));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new FileOperationException("Error exporting attendance report", e);
        }
    }

    // ==========================
    // Utilities
    // ==========================
    private static void validateFile(Path filePath) throws FileOperationException {
        if (filePath == null) throw new FileOperationException("File path cannot be null");
        if (!Files.exists(filePath)) throw new FileOperationException("File not found: " + filePath);
        if (!Files.isRegularFile(filePath)) throw new FileOperationException("Path is not a regular file: " + filePath);

        try {
            long fileSize = Files.size(filePath);
            if (fileSize > MAX_FILE_SIZE_BYTES) throw new FileOperationException("File exceeds max size: " + fileSize + " bytes");
            if (fileSize == 0) throw new FileOperationException("File is empty");
        } catch (IOException e) {
            throw new FileOperationException("Cannot read file: " + e.getMessage(), e);
        }

        String fileName = filePath.getFileName().toString().toLowerCase();
        if (!fileName.endsWith(".csv")) throw new FileOperationException("File must have .csv extension");
    }

    private static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    private static String unescapeCsvField(String field) {
        if (field == null || field.trim().isEmpty()) return null;
        field = field.trim();
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1).replace("\"\"", "\"");
        }
        return field.isEmpty() ? null : field;
    }
}
