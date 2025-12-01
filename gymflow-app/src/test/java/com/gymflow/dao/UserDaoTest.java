package com.gymflow.dao;

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
 * Integration test for UserDao using H2 in-memory database.
 */
class UserDaoTest {
    private static DatabaseConnection dbConnection;
    private static Connection testConnection; // Keep connection open
    private UserDao userDao;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        // Reset singleton to ensure fresh connection
        DatabaseConnection.resetInstance();
        // Get database connection - use a unique database name for tests
        // DB_CLOSE_DELAY=-1 keeps database in memory as long as JVM is alive
        System.setProperty("GYMFLOW_DB_URL", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
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
            
            // Insert roles (H2-compatible syntax - use INSERT IGNORE)
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
            
            // Insert test user
            String passwordHash = PasswordHasher.sha256("password123");
            try {
                stmt.execute(String.format("""
                    INSERT INTO users (id, role_id, username, password_hash, full_name, email) 
                    VALUES (1, 1, 'testmember', '%s', 'Test Member', 'test@example.com')
                    """, passwordHash));
            } catch (Exception e) {
                // Ignore if already exists
            }
            
            // Verify data was inserted
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'testmember'");
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) {
                throw new RuntimeException("Test user was not inserted into database");
            }
        }
        // Connection stays open to keep database in memory
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
    }

    @Test
    void testFindByUsername_ExistingUser_ReturnsUser() {
        Optional<User> result = userDao.findByUsername("testmember");
        
        assertTrue(result.isPresent(), "User should be found");
        User user = result.get();
        assertEquals("testmember", user.getUsername());
        assertEquals("Test Member", user.getFullName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(Role.MEMBER, user.getRole());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testFindByUsername_NonExistentUser_ReturnsEmpty() {
        Optional<User> result = userDao.findByUsername("nonexistent");
        
        assertFalse(result.isPresent(), "User should not be found");
    }

    @Test
    void testFindByUsername_CaseSensitive() {
        Optional<User> result = userDao.findByUsername("TESTMEMBER");
        
        // Username lookup should be case-sensitive
        assertFalse(result.isPresent(), "Username lookup should be case-sensitive");
    }
}

