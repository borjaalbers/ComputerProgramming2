package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.ClassSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ClassSessionDao for database operations on ClassSession entities.
 */
public class ClassSessionDaoImpl implements ClassSessionDao {
    private final DatabaseConnection dbConnection;

    public ClassSessionDaoImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Optional<ClassSession> findById(long id) {
        String sql = """
            SELECT id, trainer_id, title, schedule_timestamp, capacity, workout_plan_id
            FROM class_sessions
            WHERE id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToClassSession(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding class session by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<ClassSession> findByTrainerId(long trainerId) {
        String sql = """
            SELECT id, trainer_id, title, schedule_timestamp, capacity, workout_plan_id
            FROM class_sessions
            WHERE trainer_id = ?
            ORDER BY schedule_timestamp ASC
            """;

        List<ClassSession> sessions = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, trainerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToClassSession(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding class sessions by trainer ID: " + e.getMessage());
            e.printStackTrace();
        }

        return sessions;
    }

    @Override
    public List<ClassSession> findUpcoming() {
        String sql = """
            SELECT id, trainer_id, title, schedule_timestamp, capacity, workout_plan_id
            FROM class_sessions
            WHERE schedule_timestamp > CURRENT_TIMESTAMP
            ORDER BY schedule_timestamp ASC
            """;

        List<ClassSession> sessions = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToClassSession(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding upcoming class sessions: " + e.getMessage());
            e.printStackTrace();
        }

        return sessions;
    }

    @Override
    public Optional<ClassSession> create(ClassSession classSession) {
        String sql = """
            INSERT INTO class_sessions (trainer_id, title, schedule_timestamp, capacity, workout_plan_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, classSession.getTrainerId());
            stmt.setString(2, classSession.getTitle());
            stmt.setTimestamp(3, Timestamp.valueOf(classSession.getScheduleTimestamp()));
            stmt.setInt(4, classSession.getCapacity());
            if (classSession.getWorkoutPlanId() != null) {
                stmt.setLong(5, classSession.getWorkoutPlanId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                return Optional.empty();
            }

            // Get the generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);

                    ClassSession created = new ClassSession(
                        id,
                        classSession.getTrainerId(),
                        classSession.getTitle(),
                        classSession.getScheduleTimestamp(),
                        classSession.getCapacity(),
                        classSession.getWorkoutPlanId()
                    );
                    return Optional.of(created);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating class session: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean update(ClassSession classSession) {
        String sql = """
            UPDATE class_sessions
            SET title = ?, schedule_timestamp = ?, capacity = ?, workout_plan_id = ?
            WHERE id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classSession.getTitle());
            stmt.setTimestamp(2, Timestamp.valueOf(classSession.getScheduleTimestamp()));
            stmt.setInt(3, classSession.getCapacity());
            if (classSession.getWorkoutPlanId() != null) {
                stmt.setLong(4, classSession.getWorkoutPlanId());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            stmt.setLong(5, classSession.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating class session: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM class_sessions WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting class session: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private ClassSession mapResultSetToClassSession(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long trainerId = rs.getLong("trainer_id");
        String title = rs.getString("title");
        Timestamp scheduleTimestamp = rs.getTimestamp("schedule_timestamp");
        LocalDateTime schedule = scheduleTimestamp != null ? scheduleTimestamp.toLocalDateTime() : null;
        int capacity = rs.getInt("capacity");
        Long workoutPlanId = rs.getObject("workout_plan_id", Long.class);

        return new ClassSession(id, trainerId, title, schedule, capacity, workoutPlanId);
    }
}

