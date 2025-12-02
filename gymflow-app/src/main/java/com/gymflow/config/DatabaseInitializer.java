package com.gymflow.config;

import com.gymflow.security.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        // Create workout_completions table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS workout_completions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                workout_plan_id INT,
                member_id INT,
                class_session_id INT,
                completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                notes TEXT,
                FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id),
                FOREIGN KEY (member_id) REFERENCES users(id),
                FOREIGN KEY (class_session_id) REFERENCES class_sessions(id)
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
        
        // Create workout_completions table if it doesn't exist
        try {
            stmt.executeQuery("SELECT id FROM workout_completions LIMIT 1");
        } catch (SQLException e) {
            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS workout_completions (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        workout_plan_id INT,
                        member_id INT,
                        class_session_id INT,
                        completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        notes TEXT,
                        FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id),
                        FOREIGN KEY (member_id) REFERENCES users(id),
                        FOREIGN KEY (class_session_id) REFERENCES class_sessions(id)
                    )
                    """);
                System.out.println("Created workout_completions table");
            } catch (SQLException ex) {
                System.out.println("Could not create workout_completions table: " + ex.getMessage());
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

        // Insert sample workout plans (assigned to member by trainer)
        insertSampleWorkoutPlans(stmt);

        // Insert sample class sessions
        insertSampleClassSessions(stmt);

        // Insert sample equipment
        insertSampleEquipment(stmt);

        // Insert sample attendance records
        insertSampleAttendanceRecords(stmt);
    }

    /**
     * Inserts sample workout plans for testing.
     * Assumes member_demo has id=1 and trainer_demo has id=2.
     */
    private void insertSampleWorkoutPlans(Statement stmt) throws SQLException {
        int workoutPlansInserted = 0;

        // Check if workout plans already exist
        try {
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM workout_plans");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Workout plans already exist, skipping seed data");
                return;
            }
        } catch (SQLException e) {
            // Table might not exist yet, continue
        }

        // Workout Plan 1: Beginner Full Body
        try {
            stmt.execute("""
                INSERT INTO workout_plans (id, member_id, trainer_id, title, description, difficulty, 
                    muscle_group, workout_type, duration_minutes, equipment_needed, target_sets, target_reps, rest_seconds)
                VALUES (1, 1, 2, 'Beginner Full Body Workout', 
                    'A comprehensive full-body workout perfect for beginners. Focuses on building strength and endurance.',
                    'Beginner', 'Full Body', 45, 'Dumbbells, Bench, Resistance Bands', 3, 12, 60)
                """);
            workoutPlansInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Workout Plan 2: Upper Body Strength
        try {
            stmt.execute("""
                INSERT INTO workout_plans (id, member_id, trainer_id, title, description, difficulty, 
                    muscle_group, workout_type, duration_minutes, equipment_needed, target_sets, target_reps, rest_seconds)
                VALUES (2, 1, 2, 'Upper Body Strength Training', 
                    'Target your chest, back, shoulders, and arms with this strength-focused routine.',
                    'Intermediate', 'Upper Body', 50, 'Barbell, Bench, Pull-up Bar', 4, 8, 90)
                """);
            workoutPlansInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Workout Plan 3: Cardio HIIT
        try {
            stmt.execute("""
                INSERT INTO workout_plans (id, member_id, trainer_id, title, description, difficulty, 
                    muscle_group, workout_type, duration_minutes, equipment_needed, target_sets, target_reps, rest_seconds)
                VALUES (3, 1, 2, 'Cardio HIIT Session', 
                    'High-intensity interval training to boost cardiovascular fitness and burn calories.',
                    'Intermediate', 'Cardio', 30, 'Treadmill, Jump Rope, Mat', 5, 20, 30)
                """);
            workoutPlansInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Workout Plan 4: Lower Body Power
        try {
            stmt.execute("""
                INSERT INTO workout_plans (id, member_id, trainer_id, title, description, difficulty, 
                    muscle_group, workout_type, duration_minutes, equipment_needed, target_sets, target_reps, rest_seconds)
                VALUES (4, 1, 2, 'Lower Body Power Workout', 
                    'Build explosive power in your legs and glutes with this lower body focused routine.',
                    'Advanced', 'Lower Body', 55, 'Squat Rack, Barbell, Kettlebells', 4, 6, 120)
                """);
            workoutPlansInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Workout Plan 5: Core Stability
        try {
            stmt.execute("""
                INSERT INTO workout_plans (id, member_id, trainer_id, title, description, difficulty, 
                    muscle_group, workout_type, duration_minutes, equipment_needed, target_sets, target_reps, rest_seconds)
                VALUES (5, 1, 2, 'Core Stability & Balance', 
                    'Strengthen your core muscles and improve balance with bodyweight and stability exercises.',
                    'Beginner', 'Core', 25, 'Yoga Mat, Stability Ball, Resistance Bands', 3, 15, 45)
                """);
            workoutPlansInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        if (workoutPlansInserted > 0) {
            System.out.println("Inserted " + workoutPlansInserted + " sample workout plan(s)");
        }
    }

    /**
     * Inserts sample class sessions for testing.
     * Assumes trainer_demo has id=2.
     */
    private void insertSampleClassSessions(Statement stmt) throws SQLException {
        int classSessionsInserted = 0;

        // Check if class sessions already exist
        try {
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM class_sessions");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Class sessions already exist, skipping seed data");
                return;
            }
        } catch (SQLException e) {
            // Table might not exist yet, continue
        }

        // Get current timestamp and create future class sessions
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // Class Session 1: Morning Yoga (tomorrow at 8 AM)
        try {
            LocalDateTime sessionTime1 = now.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
            stmt.execute(String.format("""
                INSERT INTO class_sessions (id, trainer_id, title, schedule_timestamp, capacity, workout_plan_id)
                VALUES (1, 2, 'Morning Yoga Class', TIMESTAMP '%s', 15, 5)
                """, sessionTime1.format(formatter)));
            classSessionsInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Class Session 2: HIIT Training (tomorrow at 6 PM)
        try {
            LocalDateTime sessionTime2 = now.plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);
            stmt.execute(String.format("""
                INSERT INTO class_sessions (id, trainer_id, title, schedule_timestamp, capacity, workout_plan_id)
                VALUES (2, 2, 'Evening HIIT Training', TIMESTAMP '%s', 20, 3)
                """, sessionTime2.format(formatter)));
            classSessionsInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Class Session 3: Strength Training (2 days from now at 10 AM)
        try {
            LocalDateTime sessionTime3 = now.plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
            stmt.execute(String.format("""
                INSERT INTO class_sessions (id, trainer_id, title, schedule_timestamp, capacity, workout_plan_id)
                VALUES (3, 2, 'Strength Training Workshop', TIMESTAMP '%s', 12, 2)
                """, sessionTime3.format(formatter)));
            classSessionsInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Class Session 4: Cardio Blast (3 days from now at 7 AM)
        try {
            LocalDateTime sessionTime4 = now.plusDays(3).withHour(7).withMinute(0).withSecond(0).withNano(0);
            stmt.execute(String.format("""
                INSERT INTO class_sessions (id, trainer_id, title, schedule_timestamp, capacity, workout_plan_id)
                VALUES (4, 2, 'Early Morning Cardio Blast', TIMESTAMP '%s', 25, 3)
                """, sessionTime4.format(formatter)));
            classSessionsInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Class Session 5: Full Body Circuit (4 days from now at 5 PM)
        try {
            LocalDateTime sessionTime5 = now.plusDays(4).withHour(17).withMinute(0).withSecond(0).withNano(0);
            stmt.execute(String.format("""
                INSERT INTO class_sessions (id, trainer_id, title, schedule_timestamp, capacity, workout_plan_id)
                VALUES (5, 2, 'Full Body Circuit Training', TIMESTAMP '%s', 18, 1)
                """, sessionTime5.format(formatter)));
            classSessionsInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        if (classSessionsInserted > 0) {
            System.out.println("Inserted " + classSessionsInserted + " sample class session(s)");
        }
    }

    /**
     * Inserts sample equipment for testing.
     */
    private void insertSampleEquipment(Statement stmt) throws SQLException {
        int equipmentInserted = 0;

        // Check if equipment already exists
        try {
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM equipment");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Equipment already exists, skipping seed data");
                return;
            }
        } catch (SQLException e) {
            // Table might not exist yet, continue
        }

        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate lastYear = today.minusYears(1);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Equipment 1: Treadmill
        try {
            stmt.execute(String.format("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (1, 'Treadmill #1', 'AVAILABLE', DATE '%s')
                """, lastMonth.format(dateFormatter)));
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Equipment 2: Bench Press
        try {
            stmt.execute(String.format("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (2, 'Adjustable Bench Press', 'AVAILABLE', DATE '%s')
                """, lastMonth.plusDays(5).format(dateFormatter)));
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Equipment 3: Dumbbell Set
        try {
            stmt.execute("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (3, 'Dumbbell Set (5-50 lbs)', 'AVAILABLE', NULL)
                """);
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Equipment 4: Squat Rack
        try {
            stmt.execute(String.format("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (4, 'Power Squat Rack', 'AVAILABLE', DATE '%s')
                """, lastMonth.minusDays(10).format(dateFormatter)));
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Equipment 5: Stationary Bike (needs service)
        try {
            stmt.execute(String.format("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (5, 'Stationary Bike #2', 'MAINTENANCE', DATE '%s')
                """, lastYear.format(dateFormatter)));
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Equipment 6: Pull-up Bar
        try {
            stmt.execute("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (6, 'Wall-Mounted Pull-up Bar', 'AVAILABLE', NULL)
                """);
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Equipment 7: Kettlebell Set
        try {
            stmt.execute(String.format("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (7, 'Kettlebell Set (10-40 kg)', 'AVAILABLE', DATE '%s')
                """, lastMonth.plusDays(15).format(dateFormatter)));
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Equipment 8: Rowing Machine (in use)
        try {
            stmt.execute(String.format("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (8, 'Rowing Machine', 'IN_USE', DATE '%s')
                """, lastMonth.minusDays(5).format(dateFormatter)));
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Equipment 9: Yoga Mats
        try {
            stmt.execute("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (9, 'Yoga Mat Set (20 mats)', 'AVAILABLE', NULL)
                """);
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Equipment 10: Resistance Bands
        try {
            stmt.execute("""
                INSERT INTO equipment (id, name, status, last_service)
                VALUES (10, 'Resistance Band Set', 'AVAILABLE', NULL)
                """);
            equipmentInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        if (equipmentInserted > 0) {
            System.out.println("Inserted " + equipmentInserted + " sample equipment item(s)");
        }
    }

    /**
     * Inserts sample attendance records for testing.
     * Assumes member_demo has id=1.
     */
    private void insertSampleAttendanceRecords(Statement stmt) throws SQLException {
        int attendanceRecordsInserted = 0;

        // Check if attendance records already exist
        try {
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM attendance_records");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Attendance records already exist, skipping seed data");
                return;
            }
        } catch (SQLException e) {
            // Table might not exist yet, continue
        }

        // Register member for class session 1 (Morning Yoga)
        try {
            stmt.execute("""
                INSERT INTO attendance_records (id, session_id, member_id, attended)
                VALUES (1, 1, 1, FALSE)
                """);
            attendanceRecordsInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Register member for class session 2 (Evening HIIT) - already attended
        try {
            stmt.execute("""
                INSERT INTO attendance_records (id, session_id, member_id, attended)
                VALUES (2, 2, 1, TRUE)
                """);
            attendanceRecordsInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        // Register member for class session 3 (Strength Training)
        try {
            stmt.execute("""
                INSERT INTO attendance_records (id, session_id, member_id, attended)
                VALUES (3, 3, 1, FALSE)
                """);
            attendanceRecordsInserted++;
        } catch (SQLException e) {
            // Already exists, ignore
        }

        if (attendanceRecordsInserted > 0) {
            System.out.println("Inserted " + attendanceRecordsInserted + " sample attendance record(s)");
        }
    }
}

