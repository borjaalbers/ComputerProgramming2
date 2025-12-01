package com.gymflow.service;

import com.gymflow.model.ClassSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Defines business operations for class session management.
 */
public interface ClassScheduleService {
    /**
     * Creates a new class session.
     *
     * @param trainerId the trainer ID leading the class
     * @param title the class title
     * @param scheduleTimestamp when the class is scheduled
     * @param capacity the maximum number of attendees
     * @return Optional containing the created ClassSession if successful, empty otherwise
     */
    Optional<ClassSession> createClassSession(long trainerId, String title, 
                                             LocalDateTime scheduleTimestamp, int capacity);

    /**
     * Gets all class sessions for a specific trainer.
     *
     * @param trainerId the trainer ID
     * @return list of class sessions for the trainer
     */
    List<ClassSession> getClassSessionsByTrainer(long trainerId);

    /**
     * Gets all upcoming class sessions.
     *
     * @return list of upcoming class sessions
     */
    List<ClassSession> getUpcomingClassSessions();

    /**
     * Gets a class session by its ID.
     *
     * @param sessionId the class session ID
     * @return Optional containing the ClassSession if found, empty otherwise
     */
    Optional<ClassSession> getClassSessionById(long sessionId);

    /**
     * Updates an existing class session.
     *
     * @param sessionId the class session ID
     * @param title the new title (can be null to keep existing)
     * @param scheduleTimestamp the new schedule time (can be null to keep existing)
     * @param capacity the new capacity (use -1 to keep existing)
     * @return true if update was successful, false otherwise
     */
    boolean updateClassSession(long sessionId, String title, LocalDateTime scheduleTimestamp, int capacity);

    /**
     * Deletes a class session.
     *
     * @param sessionId the class session ID
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteClassSession(long sessionId);
}

