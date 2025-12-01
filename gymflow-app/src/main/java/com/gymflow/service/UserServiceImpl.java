package com.gymflow.service;

import com.gymflow.dao.UserDao;
import com.gymflow.dao.UserDaoImpl;
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
    public Optional<User> createUser(String username, String password, String fullName, String email, Role role) {
        // Hash the password
        String passwordHash = PasswordHasher.sha256(password);

        // Create user via DAO
        return userDao.create(username, passwordHash, fullName, email, role);
    }
}

