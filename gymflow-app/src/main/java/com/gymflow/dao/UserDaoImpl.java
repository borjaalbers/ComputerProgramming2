package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.Role;
import com.gymflow.model.User;
import com.gymflow.model.UserFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JDBC implementation of UserDao for database operations on User entities.
 */
public class UserDaoImpl implements UserDao {
    private final DatabaseConnection dbConnection;

    public UserDaoImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Finds a user by username. Joins with roles table to get role information.
     *
     * @param username the username to search for
     * @return Optional containing the User if found, empty otherwise
     */
    @Override
    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.email, u.created_at, r.name as role_name
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.username = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    String roleName = rs.getString("role_name");

                    Role role = Role.fromString(roleName);
                    User user = UserFactory.createUser(role, id, username, fullName, email, createdAt);

                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            // Log error (TODO: add logging framework)
            System.err.println("Error finding user by username: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

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
    @Override
    public Optional<User> create(String username, String passwordHash, String fullName, String email, Role role) {
        // First check if username already exists
        if (findByUsername(username).isPresent()) {
            return Optional.empty();
        }

        // Get role_id from database
        String roleIdSql = "SELECT id FROM roles WHERE name = ?";
        int roleId = -1;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement roleStmt = conn.prepareStatement(roleIdSql)) {

            roleStmt.setString(1, role.name());
            try (ResultSet rs = roleStmt.executeQuery()) {
                if (rs.next()) {
                    roleId = rs.getInt("id");
                } else {
                    System.err.println("Role not found: " + role.name());
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding role: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }

        // Insert new user
        String insertSql = """
            INSERT INTO users (role_id, username, password_hash, full_name, email)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, roleId);
            stmt.setString(2, username);
            stmt.setString(3, passwordHash);
            stmt.setString(4, fullName);
            stmt.setString(5, email);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                return Optional.empty();
            }

            // Get the generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    LocalDateTime createdAt = LocalDateTime.now();

                    // Create and return the User object
                    User user = UserFactory.createUser(role, id, username, fullName, email, createdAt);
                    System.out.println("User created successfully: " + username);
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }
}

