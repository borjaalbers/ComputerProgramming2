package com.gymflow.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a workout plan assigned to a member by a trainer.
 * 
 * <p>A workout plan contains exercises and instructions tailored to a specific
 * member's fitness goals and capabilities. Each plan is created by a trainer
 * and assigned to a member.</p>
 */
public class WorkoutPlan {
    private final long id;
    private final long memberId;
    private final long trainerId;
    private String title;
    private String description;
    private String difficulty;
    private final LocalDateTime createdAt;

    /**
     * Creates a new WorkoutPlan.
     *
     * @param id the unique identifier
     * @param memberId the ID of the member this plan is assigned to
     * @param trainerId the ID of the trainer who created this plan
     * @param title the title of the workout plan
     * @param description a detailed description of the workout plan
     * @param difficulty the difficulty level (e.g., "Beginner", "Intermediate", "Advanced")
     * @param createdAt when this plan was created
     */
    public WorkoutPlan(long id, long memberId, long trainerId, String title, 
                      String description, String difficulty, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.trainerId = trainerId;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getMemberId() {
        return memberId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "WorkoutPlan{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", trainerId=" + trainerId +
                ", title='" + title + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkoutPlan that = (WorkoutPlan) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

