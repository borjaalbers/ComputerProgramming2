package com.gymflow.model;

import java.time.LocalDateTime;

/**
 * Administrator with full system permissions.
 */
public class Administrator extends User {

    public Administrator(long id,
                         String username,
                         String fullName,
                         String email,
                         LocalDateTime createdAt) {
        super(id, username, fullName, email, Role.ADMIN, createdAt);
    }

    @Override
    public String getRoleDisplayName() {
        return "Administrator";
    }
}

