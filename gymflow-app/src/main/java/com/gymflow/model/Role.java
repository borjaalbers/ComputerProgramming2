package com.gymflow.model;

/**
 * High-level roles supported by the system.
 *
 * These values correspond to the logical roles stored in the {@code roles}
 * database table (e.g. MEMBER, TRAINER, ADMIN).
 */
public enum Role {
    MEMBER,
    TRAINER,
    ADMIN;

    /**
     * Utility to create a {@link Role} from a case-insensitive name coming
     * from the database or configuration.
     */
    public static Role fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }
        return Role.valueOf(name.trim().toUpperCase());
    }
}



