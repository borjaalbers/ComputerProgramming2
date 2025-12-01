package com.gymflow.model;

/**
 * Wrapper class to track workout plan source (direct assignment or from class).
 */
public class WorkoutPlanWithSource {
    private final WorkoutPlan workoutPlan;
    private final ClassSession sourceClass; // null if direct assignment
    private final boolean isFromClass;

    public WorkoutPlanWithSource(WorkoutPlan workoutPlan, ClassSession sourceClass) {
        this.workoutPlan = workoutPlan;
        this.sourceClass = sourceClass;
        this.isFromClass = sourceClass != null;
    }

    public WorkoutPlan getWorkoutPlan() {
        return workoutPlan;
    }

    public ClassSession getSourceClass() {
        return sourceClass;
    }

    public boolean isFromClass() {
        return isFromClass;
    }

    public String getSourceDisplay() {
        if (isFromClass && sourceClass != null) {
            return "Class: " + sourceClass.getTitle();
        }
        return "Direct Assignment";
    }
}

