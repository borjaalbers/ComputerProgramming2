package com.gymflow.service;

import com.gymflow.model.Role;
import com.gymflow.model.User;

import java.util.Optional;

/**
 * Service interface for user management operations.
 */
public interface UserService {
    /**
     * Creates a new user account.
     *
     * @param username the username (must be unique)
     * @param password the plain text password (will be hashed)
     * @param fullName the user's full name
     * @param email the user's email
     * @param role the user's role
     * @return Optional containing the created User if successful, empty if username already exists
     */
    Optional<User> createUser(String username, String password, String fullName, String email, Role role);
}

