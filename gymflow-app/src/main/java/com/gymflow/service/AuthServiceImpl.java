package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.dao.UserDao;
import com.gymflow.dao.UserDaoImpl;
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
    public Optional<User> authenticate(String username, String password) {
        // First, find the user by username
        Optional<User> userOpt = userDao.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        // Get the stored password hash from database
        String storedPasswordHash = getPasswordHash(username);
        if (storedPasswordHash == null) {
            return Optional.empty();
        }

        // Hash the provided password and compare
        String providedPasswordHash = PasswordHasher.sha256(password);
        
        if (storedPasswordHash.equals(providedPasswordHash)) {
            return userOpt;
        }

        return Optional.empty();
    }

    /**
     * Retrieves the password hash for a given username from the database.
     *
     * @param username the username
     * @return the password hash, or null if user not found
     */
    private String getPasswordHash(String username) {
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
        }

        return null;
    }
}

