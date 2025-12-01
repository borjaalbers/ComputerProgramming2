package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.WorkoutPlan;

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
 * JDBC implementation of WorkoutPlanDao for database operations on WorkoutPlan entities.
 */
public class WorkoutPlanDaoImpl implements WorkoutPlanDao {
    private final DatabaseConnection dbConnection;

    public WorkoutPlanDaoImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Optional<WorkoutPlan> findById(long id) {
        String sql = """
            SELECT id, member_id, trainer_id, title, description, difficulty, created_at
            FROM workout_plans
            WHERE id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToWorkoutPlan(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding workout plan by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<WorkoutPlan> findByMemberId(long memberId) {
        String sql = """
            SELECT id, member_id, trainer_id, title, description, difficulty, created_at
            FROM workout_plans
            WHERE member_id = ?
            ORDER BY created_at DESC
            """;

        List<WorkoutPlan> plans = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    plans.add(mapResultSetToWorkoutPlan(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding workout plans by member ID: " + e.getMessage());
            e.printStackTrace();
        }

        return plans;
    }

    @Override
    public List<WorkoutPlan> findByTrainerId(long trainerId) {
        String sql = """
            SELECT id, member_id, trainer_id, title, description, difficulty, created_at
            FROM workout_plans
            WHERE trainer_id = ?
            ORDER BY created_at DESC
            """;

        List<WorkoutPlan> plans = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, trainerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    plans.add(mapResultSetToWorkoutPlan(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding workout plans by trainer ID: " + e.getMessage());
            e.printStackTrace();
        }

        return plans;
    }

    @Override
    public Optional<WorkoutPlan> create(WorkoutPlan workoutPlan) {
        String sql = """
            INSERT INTO workout_plans (member_id, trainer_id, title, description, difficulty)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, workoutPlan.getMemberId());
            stmt.setLong(2, workoutPlan.getTrainerId());
            stmt.setString(3, workoutPlan.getTitle());
            stmt.setString(4, workoutPlan.getDescription());
            stmt.setString(5, workoutPlan.getDifficulty());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                return Optional.empty();
            }

            // Get the generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    LocalDateTime createdAt = LocalDateTime.now();

                    WorkoutPlan created = new WorkoutPlan(
                        id,
                        workoutPlan.getMemberId(),
                        workoutPlan.getTrainerId(),
                        workoutPlan.getTitle(),
                        workoutPlan.getDescription(),
                        workoutPlan.getDifficulty(),
                        createdAt
                    );
                    return Optional.of(created);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating workout plan: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean update(WorkoutPlan workoutPlan) {
        String sql = """
            UPDATE workout_plans
            SET title = ?, description = ?, difficulty = ?
            WHERE id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, workoutPlan.getTitle());
            stmt.setString(2, workoutPlan.getDescription());
            stmt.setString(3, workoutPlan.getDifficulty());
            stmt.setLong(4, workoutPlan.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating workout plan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM workout_plans WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting workout plan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private WorkoutPlan mapResultSetToWorkoutPlan(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long memberId = rs.getLong("member_id");
        long trainerId = rs.getLong("trainer_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String difficulty = rs.getString("difficulty");
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : LocalDateTime.now();

        return new WorkoutPlan(id, memberId, trainerId, title, description, difficulty, createdAt);
    }
}

