package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.dao.UserDao;
import com.gymflow.dao.UserDaoImpl;
import com.gymflow.exception.AuthenticationException;
import com.gymflow.exception.DataAccessException;
import com.gymflow.model.User;
import com.gymflow.security.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Implementation of AuthService for user authentication.
 */
public class AuthServiceImpl implements AuthService {
    private final UserDao userDao;
    private final DatabaseConnection dbConnection;

    public AuthServiceImpl() {
        this.userDao = new UserDaoImpl();
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Authenticates a user by verifying username and password.
     * Retrieves the user from database and compares password hash.
     *
     * @param username the username
     * @param password the plain text password
     * @return Optional containing the User if authentication succeeds, empty otherwise
     */
    @Override
    public Optional<User> authenticate(String username, String password) throws AuthenticationException {
        try {
            System.out.println("Attempting authentication for user: " + username);
            
            // First, find the user by username
            Optional<User> userOpt = userDao.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                System.out.println("Authentication failed: User '" + username + "' not found in database");
                return Optional.empty();
            }

            System.out.println("User found: " + username);

            // Get the stored password hash from database
            String storedPasswordHash = getPasswordHash(username);
            if (storedPasswordHash == null || storedPasswordHash.isEmpty()) {
                System.out.println("Authentication failed: Could not retrieve password hash for '" + username + "'");
                return Optional.empty();
            }

            System.out.println("Stored password hash length: " + storedPasswordHash.length());

            // Hash the provided password and compare
            String providedPasswordHash = PasswordHasher.sha256(password);
            System.out.println("Provided password hash length: " + providedPasswordHash.length());
            
            if (storedPasswordHash.equals(providedPasswordHash)) {
                System.out.println("Authentication successful for user: " + username);
                return userOpt;
            } else {
                System.out.println("Authentication failed: Password hash mismatch for user '" + username + "'");
                System.out.println("Stored hash (first 20 chars): " + storedPasswordHash.substring(0, Math.min(20, storedPasswordHash.length())));
                System.out.println("Provided hash (first 20 chars): " + providedPasswordHash.substring(0, Math.min(20, providedPasswordHash.length())));
            }

            return Optional.empty();
        } catch (DataAccessException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            e.printStackTrace();
            throw new AuthenticationException("Failed to authenticate user due to database error", e);
        } catch (Exception e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Retrieves the password hash for a given username from the database.
     *
     * @param username the username
     * @return the password hash, or null if user not found
     */
    private String getPasswordHash(String username) throws DataAccessException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving password hash: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to retrieve password hash for user: " + username, e);
        }

        return null;
    }
}

