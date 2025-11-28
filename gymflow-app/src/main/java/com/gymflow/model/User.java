package com.gymflow.model;

/**
 * Base representation of a GymFlow user.
 */
public abstract class User {
    private final long id;
    private final String username;
    private final String fullName;

    protected User(long id, String username, String fullName) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
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
}
