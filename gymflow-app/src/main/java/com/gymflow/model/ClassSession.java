package com.gymflow.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a scheduled fitness class session led by a trainer.
 * 
 * <p>Class sessions are group fitness activities with a specific time, capacity,
 * and trainer. Members can register to attend these sessions.</p>
 */
public class ClassSession {
    private final long id;
    private final long trainerId;
    private String title;
    private LocalDateTime scheduleTimestamp;
    private int capacity;
    private Long workoutPlanId;

    /**
     * Creates a new ClassSession.
     *
     * @param id the unique identifier
     * @param trainerId the ID of the trainer leading this session
     * @param title the title/name of the class (e.g., "Yoga Basics", "HIIT Training")
     * @param scheduleTimestamp when this class session is scheduled
     * @param capacity the maximum number of members who can attend
     */
    public ClassSession(long id, long trainerId, String title, 
                       LocalDateTime scheduleTimestamp, int capacity) {
        this(id, trainerId, title, scheduleTimestamp, capacity, null);
    }

    /**
     * Creates a new ClassSession with workout plan.
     *
     * @param id the unique identifier
     * @param trainerId the ID of the trainer leading this session
     * @param title the title/name of the class
     * @param scheduleTimestamp when this class session is scheduled
     * @param capacity the maximum number of members who can attend
     * @param workoutPlanId the ID of the associated workout plan (can be null)
     */
    public ClassSession(long id, long trainerId, String title, 
                       LocalDateTime scheduleTimestamp, int capacity, Long workoutPlanId) {
        this.id = id;
        this.trainerId = trainerId;
        this.title = title;
        this.scheduleTimestamp = scheduleTimestamp;
        this.capacity = capacity;
        this.workoutPlanId = workoutPlanId;
    }

    public long getId() {
        return id;
    }

    public long getTrainerId() {
        return trainerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getScheduleTimestamp() {
        return scheduleTimestamp;
    }

    public void setScheduleTimestamp(LocalDateTime scheduleTimestamp) {
        this.scheduleTimestamp = scheduleTimestamp;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Long getWorkoutPlanId() {
        return workoutPlanId;
    }

    public void setWorkoutPlanId(Long workoutPlanId) {
        this.workoutPlanId = workoutPlanId;
    }

    /**
     * Checks if this session is in the future.
     *
     * @return true if the session is scheduled for a future time
     */
    public boolean isUpcoming() {
        return scheduleTimestamp != null && scheduleTimestamp.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if this session is in the past.
     *
     * @return true if the session has already occurred
     */
    public boolean isPast() {
        return scheduleTimestamp != null && scheduleTimestamp.isBefore(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "ClassSession{" +
                "id=" + id +
                ", trainerId=" + trainerId +
                ", title='" + title + '\'' +
                ", scheduleTimestamp=" + scheduleTimestamp +
                ", capacity=" + capacity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassSession that = (ClassSession) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

