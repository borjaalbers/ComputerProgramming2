package com.gymflow.config;

import com.gymflow.security.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the database schema and seed data on application startup.
 * Creates all required tables and inserts initial data if they don't exist.
 */
public class DatabaseInitializer {
    private final DatabaseConnection dbConnection;

    public DatabaseInitializer() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Initializes the database by creating all tables and inserting seed data.
     * This method is idempotent - safe to call multiple times.
     *
     * @throws SQLException if database initialization fails
     */
    public void initialize() throws SQLException {
        Connection conn = dbConnection.getConnection();
        try (Statement stmt = conn.createStatement()) {
            System.out.println("Creating database tables...");
            
            // Create tables (H2-compatible syntax)
            createTables(stmt);
            System.out.println("Database tables created successfully.");

            // Insert seed data
            System.out.println("Inserting seed data...");
            insertSeedData(stmt);
            System.out.println("Seed data inserted successfully.");
        }
        // Note: We don't close the connection here - DatabaseConnection singleton maintains it
    }

    /**
     * Creates all database tables if they don't exist.
     */
    private void createTables(Statement stmt) throws SQLException {
        // Create roles table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS roles (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(50) UNIQUE NOT NULL
            )
            """);

        // Create users table
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

        // Create workout_plans table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS workout_plans (
                id INT AUTO_INCREMENT PRIMARY KEY,
                member_id INT,
                trainer_id INT,
                title VARCHAR(150) NOT NULL,
                description TEXT,
                difficulty VARCHAR(50),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (member_id) REFERENCES users(id),
                FOREIGN KEY (trainer_id) REFERENCES users(id)
            )
            """);

        // Create class_sessions table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS class_sessions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                trainer_id INT,
                title VARCHAR(150) NOT NULL,
                schedule_timestamp TIMESTAMP NOT NULL,
                capacity INT DEFAULT 10,
                FOREIGN KEY (trainer_id) REFERENCES users(id)
            )
            """);

        // Create attendance_records table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS attendance_records (
                id INT AUTO_INCREMENT PRIMARY KEY,
                session_id INT,
                member_id INT,
                attended BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (session_id) REFERENCES class_sessions(id),
                FOREIGN KEY (member_id) REFERENCES users(id)
            )
            """);

        // Create equipment table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS equipment (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(150) NOT NULL,
                status VARCHAR(50) DEFAULT 'AVAILABLE',
                last_service DATE
            )
            """);
    }

    /**
     * Inserts seed data (roles and test users) if they don't exist.
     */
    private void insertSeedData(Statement stmt) throws SQLException {
        // Insert roles
        int rolesInserted = 0;
        try {
            stmt.execute("INSERT INTO roles (id, name) VALUES (1, 'MEMBER')");
            rolesInserted++;
        } catch (SQLException e) {
            // Role already exists, ignore
        }
        try {
            stmt.execute("INSERT INTO roles (id, name) VALUES (2, 'TRAINER')");
            rolesInserted++;
        } catch (SQLException e) {
            // Role already exists, ignore
        }
        try {
            stmt.execute("INSERT INTO roles (id, name) VALUES (3, 'ADMIN')");
            rolesInserted++;
        } catch (SQLException e) {
            // Role already exists, ignore
        }
        if (rolesInserted > 0) {
            System.out.println("Inserted " + rolesInserted + " role(s)");
        }

        // Insert test users with hashed passwords
        // Password for all demo users: "password123"
        String passwordHash = PasswordHasher.sha256("password123");
        System.out.println("Password hash for test users: " + passwordHash.substring(0, 16) + "...");

        int usersInserted = 0;
        // Member user
        try {
            stmt.execute(String.format("""
                INSERT INTO users (id, role_id, username, password_hash, full_name, email)
                VALUES (1, 1, 'member_demo', '%s', 'Demo Member', 'member@gymflow.local')
                """, passwordHash));
            usersInserted++;
            System.out.println("Created test user: member_demo");
        } catch (SQLException e) {
            // User already exists, ignore
            System.out.println("User member_demo already exists");
        }

        // Trainer user
        try {
            stmt.execute(String.format("""
                INSERT INTO users (id, role_id, username, password_hash, full_name, email)
                VALUES (2, 2, 'trainer_demo', '%s', 'Demo Trainer', 'trainer@gymflow.local')
                """, passwordHash));
            usersInserted++;
            System.out.println("Created test user: trainer_demo");
        } catch (SQLException e) {
            // User already exists, ignore
            System.out.println("User trainer_demo already exists");
        }

        // Admin user
        try {
            stmt.execute(String.format("""
                INSERT INTO users (id, role_id, username, password_hash, full_name, email)
                VALUES (3, 3, 'admin_demo', '%s', 'Demo Admin', 'admin@gymflow.local')
                """, passwordHash));
            usersInserted++;
            System.out.println("Created test user: admin_demo");
        } catch (SQLException e) {
            // User already exists, ignore
            System.out.println("User admin_demo already exists");
        }
        
        if (usersInserted > 0) {
            System.out.println("Inserted " + usersInserted + " test user(s)");
        }
    }
}

