package com.gymflow.service;

import com.gymflow.model.ClassSession;
import com.gymflow.exception.DataAccessException; // Import this

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Defines business operations for class session management.
 */
public interface ClassScheduleService {
    /**
     * Creates a new class session.
     */
    Optional<ClassSession> createClassSession(long trainerId, String title,
                                              LocalDateTime scheduleTimestamp, int capacity) throws DataAccessException;

    /**
     * Gets all class sessions for a specific trainer.
     */
    List<ClassSession> getClassSessionsByTrainer(long trainerId) throws DataAccessException;

    /**
     * Gets all upcoming class sessions.
     */
    List<ClassSession> getUpcomingClassSessions() throws DataAccessException;

    /**
     * Gets a class session by its ID.
     */
    Optional<ClassSession> getClassSessionById(long sessionId) throws DataAccessException;

    /**
     * Updates an existing class session.
     */
    boolean updateClassSession(long sessionId, String title, LocalDateTime scheduleTimestamp, int capacity) throws DataAccessException;

    /**
     * Deletes a class session.
     */
    boolean deleteClassSession(long sessionId) throws DataAccessException;

    /**
     * Assigns a workout plan to a class session.
     */
    boolean assignWorkoutPlanToClass(long sessionId, Long workoutPlanId) throws DataAccessException;
}