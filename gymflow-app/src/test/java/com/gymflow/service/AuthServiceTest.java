package com.gymflow.service;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.Role;
import com.gymflow.model.User;
import com.gymflow.security.PasswordHasher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for AuthService using H2 in-memory database.
 */
class AuthServiceTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection; // Keep connection open
    private AuthService authService;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        // Reset singleton to ensure fresh connection
        DatabaseConnection.resetInstance();
        // Get database connection - use a unique database name for tests
        // DB_CLOSE_DELAY=-1 keeps database in memory as long as JVM is alive
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1;MODE=MySQL");
        dbConnection = DatabaseConnection.getInstance();
        
        // Set up in-memory H2 database with schema
        // Keep connection open to ensure database persists
        testConnection = dbConnection.getConnection();
        try (Statement stmt = testConnection.createStatement()) {
            
            // Create tables
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(50) UNIQUE NOT NULL
                )
                """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    role_id INT NOT NULL,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    full_name VARCHAR(150) NOT NULL,
                    email VARCHAR(150) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (role_id) REFERENCES roles(id)
                )
                """);
            
            // Clear existing data
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM roles");
            
            // Insert roles (H2-compatible syntax - use INSERT)
            try {
                stmt.execute("INSERT INTO roles (id, name) VALUES (1, 'MEMBER')");
            } catch (Exception e) {
                // Ignore if already exists
            }
            try {
                stmt.execute("INSERT INTO roles (id, name) VALUES (2, 'TRAINER')");
            } catch (Exception e) {
                // Ignore if already exists
            }
            try {
                stmt.execute("INSERT INTO roles (id, name) VALUES (3, 'ADMIN')");
            } catch (Exception e) {
                // Ignore if already exists
            }
            
            // Insert test users with different roles
            String memberPasswordHash = PasswordHasher.sha256("member123");
            String trainerPasswordHash = PasswordHasher.sha256("trainer123");
            
            try {
                stmt.execute(String.format("""
                    INSERT INTO users (id, role_id, username, password_hash, full_name, email) 
                    VALUES (1, 1, 'member1', '%s', 'Member One', 'member1@example.com')
                    """, memberPasswordHash));
            } catch (Exception e) {
                // Ignore if already exists
            }
            
            try {
                stmt.execute(String.format("""
                    INSERT INTO users (id, role_id, username, password_hash, full_name, email) 
                    VALUES (2, 2, 'trainer1', '%s', 'Trainer One', 'trainer1@example.com')
                    """, trainerPasswordHash));
            } catch (Exception e) {
                // Ignore if already exists
            }
            
            // Verify data was inserted
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username IN ('member1', 'trainer1')");
            rs.next();
            int count = rs.getInt(1);
            if (count < 2) {
                throw new RuntimeException("Test users were not inserted into database");
            }
        }
        // Commit and keep connection open to keep database in memory
        testConnection.setAutoCommit(true); // Ensure auto-commit is on
    }

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl();
    }

    @Test
    void testAuthenticate_ValidCredentials_ReturnsUser() {
        Optional<User> result = authService.authenticate("member1", "member123");
        
        assertTrue(result.isPresent(), "Authentication should succeed");
        User user = result.get();
        assertEquals("member1", user.getUsername());
        assertEquals(Role.MEMBER, user.getRole());
    }

    @Test
    void testAuthenticate_InvalidPassword_ReturnsEmpty() {
        Optional<User> result = authService.authenticate("member1", "wrongpassword");
        
        assertFalse(result.isPresent(), "Authentication should fail with wrong password");
    }

    @Test
    void testAuthenticate_NonExistentUser_ReturnsEmpty() {
        Optional<User> result = authService.authenticate("nonexistent", "password");
        
        assertFalse(result.isPresent(), "Authentication should fail for non-existent user");
    }

    @Test
    void testAuthenticate_TrainerUser_ReturnsTrainer() {
        Optional<User> result = authService.authenticate("trainer1", "trainer123");
        
        assertTrue(result.isPresent(), "Authentication should succeed");
        User user = result.get();
        assertEquals("trainer1", user.getUsername());
        assertEquals(Role.TRAINER, user.getRole());
    }
}

