package com.gymflow.dao;

import com.gymflow.model.WorkoutPlan;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for WorkoutPlan entities.
 */
public interface WorkoutPlanDao {
    /**
     * Finds a workout plan by its ID.
     *
     * @param id the workout plan ID
     * @return Optional containing the WorkoutPlan if found, empty otherwise
     */
    Optional<WorkoutPlan> findById(long id);

    /**
     * Finds all workout plans assigned to a specific member.
     *
     * @param memberId the member ID
     * @return list of workout plans for the member
     */
    List<WorkoutPlan> findByMemberId(long memberId);

    /**
     * Finds all workout plans created by a specific trainer.
     *
     * @param trainerId the trainer ID
     * @return list of workout plans created by the trainer
     */
    List<WorkoutPlan> findByTrainerId(long trainerId);

    /**
     * Creates a new workout plan in the database.
     *
     * @param workoutPlan the workout plan to create (id will be generated)
     * @return Optional containing the created WorkoutPlan with generated ID, empty if creation fails
     */
    Optional<WorkoutPlan> create(WorkoutPlan workoutPlan);

    /**
     * Updates an existing workout plan in the database.
     *
     * @param workoutPlan the workout plan to update (must have valid id)
     * @return true if update was successful, false otherwise
     */
    boolean update(WorkoutPlan workoutPlan);

    /**
     * Deletes a workout plan from the database.
     *
     * @param id the workout plan ID to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean delete(long id);
}

