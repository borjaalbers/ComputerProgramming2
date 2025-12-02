package com.gymflow.dao;

import com.gymflow.exception.DataAccessException;
import com.gymflow.model.Role;
import com.gymflow.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for user entities.
 */
public interface UserDao {
    Optional<User> findByUsername(String username) throws DataAccessException;
    
    /**
     * Creates a new user in the database.
     *
     * @param username the username (must be unique)
     * @param passwordHash the hashed password
     * @param fullName the user's full name
     * @param email the user's email
     * @param role the user's role
     * @return Optional containing the created User if successful, empty if username already exists
     * @throws DataAccessException if database operation fails
     */
    Optional<User> create(String username, String passwordHash, String fullName, String email, Role role) throws DataAccessException;
    
    /**
     * Finds a user by ID.
     *
     * @param id the user ID
     * @return Optional containing the User if found, empty otherwise
     * @throws DataAccessException if database operation fails
     */
    Optional<User> findById(long id) throws DataAccessException;
    
    /**
     * Finds all users with a specific role.
     *
     * @param role the role to filter by
     * @return list of users with the specified role
     * @throws DataAccessException if database operation fails
     */
    java.util.List<User> findByRole(Role role) throws DataAccessException;
    
    /**
     * Counts users by role.
     *
     * @param role the role to count
     * @return the number of users with the specified role
     * @throws DataAccessException if database operation fails
     */
    int countByRole(Role role) throws DataAccessException;
    
    /**
     * Gets all users from the database.
     *
     * @return list of all users
     * @throws DataAccessException if database operation fails
     */
    java.util.List<User> findAll() throws DataAccessException;
    
    /**
     * Updates a user's information.
     *
     * @param id the user ID
     * @param fullName the new full name (can be null to keep existing)
     * @param email the new email (can be null to keep existing)
     * @param role the new role (can be null to keep existing)
     * @return true if update was successful
     * @throws DataAccessException if database operation fails
     */
    boolean update(long id, String fullName, String email, Role role) throws DataAccessException;
    
    /**
     * Deletes a user from the database.
     *
     * @param id the user ID to delete
     * @return true if deletion was successful
     * @throws DataAccessException if database operation fails
     */
    boolean delete(long id) throws DataAccessException;
}
