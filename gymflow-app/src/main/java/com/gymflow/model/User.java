package com.gymflow.model;

import java.time.LocalDateTime;

/**
 * Base representation of a GymFlow user.
 *
 * <p>This abstract class captures the common fields for all user types and
 * provides a consistent API for the rest of the application. Concrete
 * subclasses (Member, Trainer, Administrator) can add additional behaviour
 * or fields as needed.</p>
 */
public abstract class User {

    private final long id;
    private final String username;
    private String fullName;
    private String email;
    private final Role role;
    private final LocalDateTime createdAt;

    protected User(long id,
                   String username,
                   String fullName,
                   String email,
                   Role role,
                   LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Human-friendly representation of the user's role that can be shown
     * directly in the UI.
     */
    public abstract String getRoleDisplayName();

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}

