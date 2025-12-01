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

            // Migrate schema (add new columns to existing tables)
            System.out.println("Migrating database schema...");
            migrateSchema(stmt);
            System.out.println("Schema migration completed.");

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
                muscle_group VARCHAR(100),
                workout_type VARCHAR(50),
                duration_minutes INT,
                equipment_needed TEXT,
                target_sets INT,
                target_reps INT,
                rest_seconds INT,
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
                workout_plan_id INT,
                FOREIGN KEY (trainer_id) REFERENCES users(id),
                FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id)
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
     * Migrates the database schema to add new columns if they don't exist.
     * Uses H2-compatible syntax by checking if column exists first.
     */
    private void migrateSchema(Statement stmt) throws SQLException {
        // Check if workout_plans table exists first
        try {
            stmt.executeQuery("SELECT id FROM workout_plans LIMIT 1");
        } catch (SQLException e) {
            // Table doesn't exist yet, skip migration (will be created with new schema)
            System.out.println("workout_plans table doesn't exist yet, skipping migration");
            return;
        }
        
        // Check if workout_plans table exists and add new columns
        try {
            // Try to query a new column - if it fails, the column doesn't exist
            stmt.executeQuery("SELECT muscle_group FROM workout_plans LIMIT 1");
        } catch (SQLException e) {
            // Column doesn't exist, add it
            try {
                stmt.execute("ALTER TABLE workout_plans ADD COLUMN muscle_group VARCHAR(100)");
                System.out.println("Added muscle_group column to workout_plans");
            } catch (SQLException ex) {
                System.out.println("Could not add muscle_group column: " + ex.getMessage());
            }
        }
        
        try {
            stmt.executeQuery("SELECT workout_type FROM workout_plans LIMIT 1");
        } catch (SQLException e) {
            try {
                stmt.execute("ALTER TABLE workout_plans ADD COLUMN workout_type VARCHAR(50)");
                System.out.println("Added workout_type column to workout_plans");
            } catch (SQLException ex) {
                System.out.println("Could not add workout_type column: " + ex.getMessage());
            }
        }
        
        try {
            stmt.executeQuery("SELECT duration_minutes FROM workout_plans LIMIT 1");
        } catch (SQLException e) {
            try {
                stmt.execute("ALTER TABLE workout_plans ADD COLUMN duration_minutes INT");
                System.out.println("Added duration_minutes column to workout_plans");
            } catch (SQLException ex) {
                System.out.println("Could not add duration_minutes column: " + ex.getMessage());
            }
        }
        
        try {
            stmt.executeQuery("SELECT equipment_needed FROM workout_plans LIMIT 1");
        } catch (SQLException e) {
            try {
                stmt.execute("ALTER TABLE workout_plans ADD COLUMN equipment_needed TEXT");
                System.out.println("Added equipment_needed column to workout_plans");
            } catch (SQLException ex) {
                System.out.println("Could not add equipment_needed column: " + ex.getMessage());
            }
        }
        
        try {
            stmt.executeQuery("SELECT target_sets FROM workout_plans LIMIT 1");
        } catch (SQLException e) {
            try {
                stmt.execute("ALTER TABLE workout_plans ADD COLUMN target_sets INT");
                System.out.println("Added target_sets column to workout_plans");
            } catch (SQLException ex) {
                System.out.println("Could not add target_sets column: " + ex.getMessage());
            }
        }
        
        try {
            stmt.executeQuery("SELECT target_reps FROM workout_plans LIMIT 1");
        } catch (SQLException e) {
            try {
                stmt.execute("ALTER TABLE workout_plans ADD COLUMN target_reps INT");
                System.out.println("Added target_reps column to workout_plans");
            } catch (SQLException ex) {
                System.out.println("Could not add target_reps column: " + ex.getMessage());
            }
        }
        
        try {
            stmt.executeQuery("SELECT rest_seconds FROM workout_plans LIMIT 1");
        } catch (SQLException e) {
            try {
                stmt.execute("ALTER TABLE workout_plans ADD COLUMN rest_seconds INT");
                System.out.println("Added rest_seconds column to workout_plans");
            } catch (SQLException ex) {
                System.out.println("Could not add rest_seconds column: " + ex.getMessage());
            }
        }
        
        // Add workout_plan_id to class_sessions if it doesn't exist
        try {
            stmt.executeQuery("SELECT workout_plan_id FROM class_sessions LIMIT 1");
        } catch (SQLException e) {
            try {
                stmt.execute("ALTER TABLE class_sessions ADD COLUMN workout_plan_id INT");
                stmt.execute("ALTER TABLE class_sessions ADD FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id)");
                System.out.println("Added workout_plan_id column to class_sessions");
            } catch (SQLException ex) {
                System.out.println("Could not add workout_plan_id column: " + ex.getMessage());
            }
        }
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

