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
}

