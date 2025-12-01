package com.gymflow.service;

import com.gymflow.model.User;

import java.util.Optional;

/**
 * Defines authentication operations for the system.
 */
public interface AuthService {
    /**
     * Authenticates a user with username and password.
     *
     * @param username the username
     * @param password the plain text password
     * @return Optional containing the authenticated User if credentials are valid, empty otherwise
     */
    Optional<User> authenticate(String username, String password);
}
