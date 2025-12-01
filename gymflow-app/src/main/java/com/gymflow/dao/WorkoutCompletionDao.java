package com.gymflow.dao;

import com.gymflow.model.WorkoutCompletion;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for WorkoutCompletion entities.
 */
public interface WorkoutCompletionDao {
    /**
     * Marks a workout plan as completed by a member.
     *
     * @param workoutPlanId the workout plan ID
     * @param memberId the member ID
     * @param classSessionId the class session ID if from a class (can be null)
     * @param notes optional notes about the completion
     * @return Optional containing the created WorkoutCompletion if successful, empty otherwise
     */
    Optional<WorkoutCompletion> markCompleted(long workoutPlanId, long memberId, Long classSessionId, String notes);

    /**
     * Checks if a member has completed a workout plan.
     *
     * @param workoutPlanId the workout plan ID
     * @param memberId the member ID
     * @return true if the workout has been completed, false otherwise
     */
    boolean isCompleted(long workoutPlanId, long memberId);

    /**
     * Gets all completions for a specific member.
     *
     * @param memberId the member ID
     * @return list of workout completions for the member
     */
    List<WorkoutCompletion> findByMemberId(long memberId);

    /**
     * Gets all completions for a specific workout plan.
     *
     * @param workoutPlanId the workout plan ID
     * @return list of workout completions for the plan
     */
    List<WorkoutCompletion> findByWorkoutPlanId(long workoutPlanId);

    /**
     * Deletes a workout completion record.
     *
     * @param workoutPlanId the workout plan ID
     * @param memberId the member ID
     * @return true if deletion was successful, false otherwise
     */
    boolean delete(long workoutPlanId, long memberId);
}

