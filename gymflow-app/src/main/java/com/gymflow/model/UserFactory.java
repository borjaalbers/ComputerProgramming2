package com.gymflow.model;

import java.time.LocalDateTime;

/**
 * Factory for constructing concrete {@link User} instances from generic data.
 *
 * <p>This will be especially useful in the DAO layer when converting rows from
 * the {@code users} table into strongly-typed model objects.</p>
 */
public final class UserFactory {

    private UserFactory() {
        // utility class
    }

    public static User createUser(Role role,
                                  long id,
                                  String username,
                                  String fullName,
                                  String email,
                                  LocalDateTime createdAt) {
        return switch (role) {
            case MEMBER -> new Member(id, username, fullName, email, createdAt);
            case TRAINER -> new Trainer(id, username, fullName, email, createdAt);
            case ADMIN -> new Administrator(id, username, fullName, email, createdAt);
        };
    }

    /**
     * Overload supporting an optional specialization used only for trainers.
     */
    public static User createUser(Role role,
                                  long id,
                                  String username,
                                  String fullName,
                                  String email,
                                  LocalDateTime createdAt,
                                  String specialization) {
        if (role == Role.TRAINER) {
            return new Trainer(id, username, fullName, email, createdAt, specialization);
        }
        return createUser(role, id, username, fullName, email, createdAt);
    }
}



