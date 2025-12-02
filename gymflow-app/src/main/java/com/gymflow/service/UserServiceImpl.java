package com.gymflow.service;

import com.gymflow.dao.UserDao;
import com.gymflow.dao.UserDaoImpl;
import com.gymflow.exception.DataAccessException;
import com.gymflow.exception.ValidationException;
import com.gymflow.model.Role;
import com.gymflow.model.User;
import com.gymflow.security.PasswordHasher;

import java.util.Optional;

/**
 * Implementation of UserService for user management operations.
 */
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
    }

    /**
     * Creates a new user account with hashed password.
     *
     * @param username the username (must be unique)
     * @param password the plain text password (will be hashed)
     * @param fullName the user's full name
     * @param email the user's email
     * @param role the user's role
     * @return Optional containing the created User if successful, empty if username already exists
     */
    @Override
    public Optional<User> createUser(String username, String password, String fullName, String email, Role role) 
        throws ValidationException, DataAccessException {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }
        if (role == null) {
            throw new ValidationException("Role cannot be null");
        }

        try {
            // Hash the password
            String passwordHash = PasswordHasher.sha256(password);

            // Create user via DAO
            return userDao.create(username.trim(), passwordHash, fullName.trim(), email.trim(), role);
        } catch (DataAccessException e) {
            System.err.println("Database error creating user: " + e.getMessage());
            throw e; // Re-throw to let controller handle it
        }
    }
}

