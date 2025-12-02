package com.gymflow.service;

import com.gymflow.exception.DataAccessException;
import com.gymflow.exception.ValidationException;
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
    Optional<User> createUser(String username, String password, String fullName, String email, Role role) 
        throws ValidationException, DataAccessException;
    
    /**
     * Gets all users in the system.
     *
     * @return list of all users
     * @throws DataAccessException if database operation fails
     */
    java.util.List<User> getAllUsers() throws DataAccessException;
    
    /**
     * Updates a user's information.
     *
     * @param id the user ID
     * @param fullName the new full name (can be null to keep existing)
     * @param email the new email (can be null to keep existing)
     * @param role the new role (can be null to keep existing)
     * @return true if update was successful
     * @throws ValidationException if validation fails
     * @throws DataAccessException if database operation fails
     */
    boolean updateUser(long id, String fullName, String email, Role role) 
        throws ValidationException, DataAccessException;
    
    /**
     * Deletes a user from the system.
     *
     * @param id the user ID to delete
     * @return true if deletion was successful
     * @throws DataAccessException if database operation fails
     */
    boolean deleteUser(long id) throws DataAccessException;
}

