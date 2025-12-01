package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.WorkoutCompletion;

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
 * JDBC implementation of WorkoutCompletionDao.
 */
public class WorkoutCompletionDaoImpl implements WorkoutCompletionDao {
    private final DatabaseConnection dbConnection;

    public WorkoutCompletionDaoImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Optional<WorkoutCompletion> markCompleted(long workoutPlanId, long memberId, Long classSessionId, String notes) {
        // Check if already completed
        if (isCompleted(workoutPlanId, memberId)) {
            System.out.println("Workout plan " + workoutPlanId + " already marked as completed by member " + memberId);
            return Optional.empty();
        }

        String sql = """
            INSERT INTO workout_completions (workout_plan_id, member_id, class_session_id, notes)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, workoutPlanId);
            stmt.setLong(2, memberId);
            if (classSessionId != null) {
                stmt.setLong(3, classSessionId);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setString(4, notes);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                return Optional.empty();
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    LocalDateTime completedAt = LocalDateTime.now();
                    return Optional.of(new WorkoutCompletion(id, workoutPlanId, memberId, classSessionId, completedAt, notes));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error marking workout as completed: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean isCompleted(long workoutPlanId, long memberId) {
        String sql = """
            SELECT id FROM workout_completions
            WHERE workout_plan_id = ? AND member_id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, workoutPlanId);
            stmt.setLong(2, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking workout completion: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<WorkoutCompletion> findByMemberId(long memberId) {
        String sql = """
            SELECT id, workout_plan_id, member_id, class_session_id, completed_at, notes
            FROM workout_completions
            WHERE member_id = ?
            ORDER BY completed_at DESC
            """;

        List<WorkoutCompletion> completions = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    completions.add(mapResultSetToCompletion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding workout completions by member ID: " + e.getMessage());
            e.printStackTrace();
        }

        return completions;
    }

    @Override
    public List<WorkoutCompletion> findByWorkoutPlanId(long workoutPlanId) {
        String sql = """
            SELECT id, workout_plan_id, member_id, class_session_id, completed_at, notes
            FROM workout_completions
            WHERE workout_plan_id = ?
            ORDER BY completed_at DESC
            """;

        List<WorkoutCompletion> completions = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, workoutPlanId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    completions.add(mapResultSetToCompletion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding workout completions by workout plan ID: " + e.getMessage());
            e.printStackTrace();
        }

        return completions;
    }

    @Override
    public boolean delete(long workoutPlanId, long memberId) {
        String sql = """
            DELETE FROM workout_completions
            WHERE workout_plan_id = ? AND member_id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, workoutPlanId);
            stmt.setLong(2, memberId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting workout completion: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private WorkoutCompletion mapResultSetToCompletion(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long workoutPlanId = rs.getLong("workout_plan_id");
        long memberId = rs.getLong("member_id");
        Long classSessionId = rs.getObject("class_session_id", Long.class);
        Timestamp completedAtTimestamp = rs.getTimestamp("completed_at");
        LocalDateTime completedAt = completedAtTimestamp != null ? completedAtTimestamp.toLocalDateTime() : LocalDateTime.now();
        String notes = rs.getString("notes");

        return new WorkoutCompletion(id, workoutPlanId, memberId, classSessionId, completedAt, notes);
    }
}

