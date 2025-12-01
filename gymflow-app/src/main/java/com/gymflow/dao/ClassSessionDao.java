package com.gymflow.dao;

import com.gymflow.model.ClassSession;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for ClassSession entities.
 */
public interface ClassSessionDao {
    /**
     * Finds a class session by its ID.
     *
     * @param id the class session ID
     * @return Optional containing the ClassSession if found, empty otherwise
     */
    Optional<ClassSession> findById(long id);

    /**
     * Finds all class sessions led by a specific trainer.
     *
     * @param trainerId the trainer ID
     * @return list of class sessions for the trainer
     */
    List<ClassSession> findByTrainerId(long trainerId);

    /**
     * Finds all upcoming class sessions (scheduled for future dates).
     *
     * @return list of upcoming class sessions
     */
    List<ClassSession> findUpcoming();

    /**
     * Creates a new class session in the database.
     *
     * @param classSession the class session to create (id will be generated)
     * @return Optional containing the created ClassSession with generated ID, empty if creation fails
     */
    Optional<ClassSession> create(ClassSession classSession);

    /**
     * Updates an existing class session in the database.
     *
     * @param classSession the class session to update (must have valid id)
     * @return true if update was successful, false otherwise
     */
    boolean update(ClassSession classSession);

    /**
     * Deletes a class session from the database.
     *
     * @param id the class session ID to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean delete(long id);
}

