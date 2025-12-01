package com.gymflow.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a member's completion of a workout plan.
 */
public class WorkoutCompletion {
    private final long id;
    private final long workoutPlanId;
    private final long memberId;
    private final Long classSessionId; // null if not from a class
    private final LocalDateTime completedAt;
    private String notes;

    public WorkoutCompletion(long id, long workoutPlanId, long memberId, 
                            Long classSessionId, LocalDateTime completedAt, String notes) {
        this.id = id;
        this.workoutPlanId = workoutPlanId;
        this.memberId = memberId;
        this.classSessionId = classSessionId;
        this.completedAt = completedAt;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public long getWorkoutPlanId() {
        return workoutPlanId;
    }

    public long getMemberId() {
        return memberId;
    }

    public Long getClassSessionId() {
        return classSessionId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkoutCompletion that = (WorkoutCompletion) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

