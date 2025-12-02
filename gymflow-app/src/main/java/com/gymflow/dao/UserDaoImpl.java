package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.exception.DataAccessException;
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
    public Optional<User> findByUsername(String username) throws com.gymflow.exception.DataAccessException {
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
            System.err.println("Error finding user by username: " + e.getMessage());
            e.printStackTrace();
            // Wrap in DataAccessException for proper error handling
            throw new DataAccessException("Failed to find user by username: " + username, e);
        }

        return Optional.empty();
    }

    /**
     * Finds a user by ID. Joins with roles table to get role information.
     *
     * @param id the user ID to search for
     * @return Optional containing the User if found, empty otherwise
     */
    @Override
    public Optional<User> findById(long id) throws com.gymflow.exception.DataAccessException {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.email, u.created_at, r.name as role_name
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long userId = rs.getLong("id");
                    String username = rs.getString("username");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    String roleName = rs.getString("role_name");

                    Role role = Role.fromString(roleName);
                    User user = UserFactory.createUser(role, userId, username, fullName, email, createdAt);

                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to find user by ID: " + id, e);
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
    public Optional<User> create(String username, String passwordHash, String fullName, String email, Role role) throws com.gymflow.exception.DataAccessException {
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
            throw new DataAccessException("Failed to find role: " + role.name(), e);
        }

        // Insert new user
        String insertSql = """
            INSERT INTO users (role_id, username, password_hash, full_name, email)
            VALUES (?, ?, ?, ?, ?)
            """;

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            // Ensure auto-commit is enabled for immediate persistence
            conn.setAutoCommit(true);
            
            try (PreparedStatement stmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, roleId);
                stmt.setString(2, username);
                stmt.setString(3, passwordHash);
                stmt.setString(4, fullName);
                stmt.setString(5, email);

                System.out.println("Attempting to create user: " + username);
                System.out.println("Password hash length: " + passwordHash.length());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    System.err.println("No rows affected when creating user: " + username);
                    return Optional.empty();
                }

                System.out.println("User inserted successfully, rows affected: " + rowsAffected);

                // Get the generated ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        LocalDateTime createdAt = LocalDateTime.now();

                        // Verify the user was actually saved by querying it back
                        Optional<User> verifyUser = findByUsername(username);
                        if (verifyUser.isPresent()) {
                            System.out.println("User created and verified successfully: " + username + " (ID: " + id + ")");
                            User user = UserFactory.createUser(role, id, username, fullName, email, createdAt);
                            return Optional.of(user);
                        } else {
                            System.err.println("User created but could not be verified: " + username);
                            return Optional.empty();
                        }
                    } else {
                        System.err.println("No generated key returned for user: " + username);
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back: " + rollbackEx.getMessage());
                }
            }
            throw new DataAccessException("Failed to create user: " + username, e);
        }
    }

    @Override
    public java.util.List<User> findByRole(Role role) throws com.gymflow.exception.DataAccessException {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.email, u.created_at, r.name as role_name
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE r.name = ?
            ORDER BY u.full_name ASC
            """;

        java.util.List<User> users = new java.util.ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    String roleName = rs.getString("role_name");

                    Role userRole = Role.fromString(roleName);
                    User user = UserFactory.createUser(userRole, id, username, fullName, email, createdAt);
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding users by role: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to find users by role: " + role.name(), e);
        }

        return users;
    }

    @Override
    public int countByRole(Role role) throws com.gymflow.exception.DataAccessException {
        String sql = """
            SELECT COUNT(*) as count
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE r.name = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting users by role: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to count users by role: " + role.name(), e);
        }

        return 0;
    }

    @Override
    public java.util.List<User> findAll() throws DataAccessException {
        String sql = """
            SELECT u.id, u.username, u.full_name, u.email, u.created_at, r.name as role_name
            FROM users u
            JOIN roles r ON u.role_id = r.id
            ORDER BY u.full_name ASC
            """;

        java.util.List<User> users = new java.util.ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String username = rs.getString("username");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    String roleName = rs.getString("role_name");

                    Role userRole = Role.fromString(roleName);
                    User user = UserFactory.createUser(userRole, id, username, fullName, email, createdAt);
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to find all users", e);
        }

        return users;
    }

    @Override
    public boolean update(long id, String fullName, String email, Role role) throws DataAccessException {
        // Build dynamic update query based on what's provided
        java.util.List<String> updates = new java.util.ArrayList<>();
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (fullName != null && !fullName.trim().isEmpty()) {
            updates.add("full_name = ?");
            params.add(fullName.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            updates.add("email = ?");
            params.add(email.trim());
        }
        if (role != null) {
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
                        throw new DataAccessException("Role not found: " + role.name());
                    }
                }
            } catch (SQLException e) {
                throw new DataAccessException("Failed to find role: " + role.name(), e);
            }
            updates.add("role_id = ?");
            params.add(roleId);
        }

        if (updates.isEmpty()) {
            return false; // Nothing to update
        }

        String sql = "UPDATE users SET " + String.join(", ", updates) + " WHERE id = ?";
        params.add(id);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Long) {
                    stmt.setLong(i + 1, (Long) param);
                }
            }

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to update user: " + id, e);
        }
    }

    @Override
    public boolean delete(long id) throws DataAccessException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to delete user: " + id, e);
        }
    }
}

