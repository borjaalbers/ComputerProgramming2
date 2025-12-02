package com.gymflow.service;

import com.gymflow.exception.DataAccessException;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.exception.FileOperationException;
import com.gymflow.exception.ValidationException;

import java.nio.file.Path;
import java.util.List;

/**
 * Service interface for WorkoutPlan-related operations,
 * including file import/export.
 */
public interface WorkoutPlanService {

    /**
     * Exports all workout plans to a CSV file at the specified path.
     *
     * @param path target file path
     * @throws FileOperationException if export fails
     */
    void exportWorkoutTemplates(Path path) throws FileOperationException, DataAccessException;

    /**
     * Imports workout plans from a CSV file at the specified path.
     *
     * @param path source file path
     * @return list of imported WorkoutPlan objects
     * @throws FileOperationException if file access fails
     * @throws ValidationException if file content is invalid
     */
    List<WorkoutPlan> importWorkoutTemplates(Path path) throws FileOperationException, ValidationException;
}
