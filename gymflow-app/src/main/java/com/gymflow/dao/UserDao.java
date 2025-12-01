package com.gymflow.dao;

import com.gymflow.model.Role;
import com.gymflow.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for user entities.
 */
public interface UserDao {
    Optional<User> findByUsername(String username);
    
    /**
     * Creates a new user in the database.
     *
     * @param username the username (must be unique)
     * @param passwordHash the hashed password
     * @param fullName the user's full name
     * @param email the user's email
     * @param role the user's role
     * @return Optional containing the created User if successful, empty if username already exists
     */
    Optional<User> create(String username, String passwordHash, String fullName, String email, Role role);
    
    /**
     * Finds a user by ID.
     *
     * @param id the user ID
     * @return Optional containing the User if found, empty otherwise
     */
    Optional<User> findById(long id);
    
    /**
     * Finds all users with a specific role.
     *
     * @param role the role to filter by
     * @return list of users with the specified role
     */
    java.util.List<User> findByRole(Role role);
    
    /**
     * Counts users by role.
     *
     * @param role the role to count
     * @return the number of users with the specified role
     */
    int countByRole(Role role);
}
