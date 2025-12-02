package com.gymflow.service;

import com.gymflow.dao.WorkoutPlanDao;
import com.gymflow.model.WorkoutPlan;
import com.gymflow.util.CsvUtil;
import com.gymflow.exception.FileOperationException;
import com.gymflow.exception.ValidationException;
import com.gymflow.exception.DataAccessException;

import java.nio.file.Path;
import java.util.List;

/**
 * Implementation of WorkoutPlanService.
 */
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private final WorkoutPlanDao workoutPlanDao;

    /**
     * Injects the DAO dependency.
     *
     * @param workoutPlanDao DAO for workout plan operations
     */
    public WorkoutPlanServiceImpl(WorkoutPlanDao workoutPlanDao) {
        this.workoutPlanDao = workoutPlanDao;
    }

    /**
     * Exports all workout plans to a CSV using CsvUtil.
     */
    @Override
    public void exportWorkoutTemplates(Path path) throws FileOperationException, DataAccessException {
        try {
            // Fetch all workout plans from DAO
            List<WorkoutPlan> plans = workoutPlanDao.findByTrainerId(0); // 0 for all trainers (adjust if needed)
            CsvUtil.exportWorkoutTemplates(plans, path);
        } catch (Exception e) {
            throw new FileOperationException("Failed to export workout templates", e);
        }
    }

    /**
     * Imports workout plans from a CSV using CsvUtil.
     */
    @Override
    public List<WorkoutPlan> importWorkoutTemplates(Path path) throws FileOperationException, ValidationException {
        return CsvUtil.importWorkoutTemplates(path);
    }
}
