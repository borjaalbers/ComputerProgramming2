package com.gymflow.service;

import com.gymflow.exception.DataAccessException;
import com.gymflow.model.WorkoutPlan;

import java.util.List;
import java.util.Optional;

/**
 * Defines business operations for workout plan management.
 */
public interface WorkoutService {
    /**
     * Creates a new workout plan for a member.
     *
     * @param memberId the member ID
     * @param trainerId the trainer ID creating the plan
     * @param title the workout plan title
     * @param description the workout plan description
     * @param difficulty the difficulty level
     * @return Optional containing the created WorkoutPlan if successful, empty otherwise
     */
    Optional<WorkoutPlan> createWorkoutPlan(long memberId, long trainerId, String title, 
                                            String description, String difficulty) throws DataAccessException;
    
    /**
     * Creates a new workout plan with all fields.
     *
     * @param memberId the member ID
     * @param trainerId the trainer ID creating the plan
     * @param title the workout plan title
     * @param description the workout plan description
     * @param difficulty the difficulty level
     * @param muscleGroup the target muscle group
     * @param workoutType the type of workout
     * @param durationMinutes the duration in minutes
     * @param equipmentNeeded the equipment needed
     * @param targetSets the target number of sets
     * @param targetReps the target number of reps
     * @param restSeconds the rest time in seconds
     * @return Optional containing the created WorkoutPlan if successful, empty otherwise
     */
    Optional<WorkoutPlan> createWorkoutPlan(long memberId, long trainerId, String title,
                                            String description, String difficulty, String muscleGroup,
                                            String workoutType, Integer durationMinutes, String equipmentNeeded,
                                            Integer targetSets, Integer targetReps, Integer restSeconds) throws DataAccessException;

    /**
     * Gets all workout plans for a specific member.
     *
     * @param memberId the member ID
     * @return list of workout plans for the member
     */
    List<WorkoutPlan> getWorkoutPlansForMember(long memberId) throws DataAccessException;

    /**
     * Gets all workout plans created by a specific trainer.
     *
     * @param trainerId the trainer ID
     * @return list of workout plans created by the trainer
     */
    List<WorkoutPlan> getWorkoutPlansByTrainer(long trainerId) throws DataAccessException;

    /**
     * Gets a workout plan by its ID.
     *
     * @param planId the workout plan ID
     * @return Optional containing the WorkoutPlan if found, empty otherwise
     */
    Optional<WorkoutPlan> getWorkoutPlanById(long planId) throws DataAccessException;

    /**
     * Updates an existing workout plan.
     *
     * @param planId the workout plan ID
     * @param title the new title (can be null to keep existing)
     * @param description the new description (can be null to keep existing)
     * @param difficulty the new difficulty (can be null to keep existing)
     * @return true if update was successful, false otherwise
     */
    boolean updateWorkoutPlan(long planId, String title, String description, String difficulty) throws DataAccessException;

    /**
     * Deletes a workout plan.
     *
     * @param planId the workout plan ID
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteWorkoutPlan(long planId) throws com.gymflow.exception.DataAccessException;
}

