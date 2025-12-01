package com.gymflow.model;

import java.time.LocalDateTime;

/**
 * Trainer responsible for creating workout plans and managing classes.
 */
public class Trainer extends User {

    // Optional extra metadata for trainers.
    private String specialization;

    public Trainer(long id,
                   String username,
                   String fullName,
                   String email,
                   LocalDateTime createdAt) {
        super(id, username, fullName, email, Role.TRAINER, createdAt);
    }

    public Trainer(long id,
                   String username,
                   String fullName,
                   String email,
                   LocalDateTime createdAt,
                   String specialization) {
        super(id, username, fullName, email, Role.TRAINER, createdAt);
        this.specialization = specialization;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    @Override
    public String getRoleDisplayName() {
        return specialization == null || specialization.isBlank()
                ? "Trainer"
                : "Trainer - " + specialization;
    }
}

