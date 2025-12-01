package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.AttendanceRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of AttendanceDao for database operations on AttendanceRecord entities.
 */
public class AttendanceDaoImpl implements AttendanceDao {
    private final DatabaseConnection dbConnection;

    public AttendanceDaoImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Optional<AttendanceRecord> findById(long id) {
        String sql = """
            SELECT id, session_id, member_id, attended
            FROM attendance_records
            WHERE id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAttendanceRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding attendance record by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<AttendanceRecord> findBySessionId(long sessionId) {
        String sql = """
            SELECT id, session_id, member_id, attended
            FROM attendance_records
            WHERE session_id = ?
            ORDER BY id ASC
            """;

        List<AttendanceRecord> records = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToAttendanceRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding attendance records by session ID: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    @Override
    public List<AttendanceRecord> findByMemberId(long memberId) {
        String sql = """
            SELECT id, session_id, member_id, attended
            FROM attendance_records
            WHERE member_id = ?
            ORDER BY id DESC
            """;

        List<AttendanceRecord> records = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToAttendanceRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding attendance records by member ID: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    @Override
    public List<AttendanceRecord> findAll() {
        String sql = """
            SELECT id, session_id, member_id, attended
            FROM attendance_records
            ORDER BY id DESC
            """;

        List<AttendanceRecord> records = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToAttendanceRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all attendance records: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    @Override
    public Optional<AttendanceRecord> markAttendance(long sessionId, long memberId, boolean attended) {
        // First check if record already exists
        String findSql = """
            SELECT id, session_id, member_id, attended
            FROM attendance_records
            WHERE session_id = ? AND member_id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findSql)) {

            findStmt.setLong(1, sessionId);
            findStmt.setLong(2, memberId);

            try (ResultSet rs = findStmt.executeQuery()) {
                if (rs.next()) {
                    // Update existing record
                    long id = rs.getLong("id");
                    String updateSql = "UPDATE attendance_records SET attended = ? WHERE id = ?";
                    
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setBoolean(1, attended);
                        updateStmt.setLong(2, id);
                        updateStmt.executeUpdate();
                    }
                    
                    return Optional.of(new AttendanceRecord(id, sessionId, memberId, attended));
                } else {
                    // Create new record
                    String insertSql = """
                        INSERT INTO attendance_records (session_id, member_id, attended)
                        VALUES (?, ?, ?)
                        """;
                    
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setLong(1, sessionId);
                        insertStmt.setLong(2, memberId);
                        insertStmt.setBoolean(3, attended);
                        
                        int rowsAffected = insertStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    long id = generatedKeys.getLong(1);
                                    return Optional.of(new AttendanceRecord(id, sessionId, memberId, attended));
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error marking attendance: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<AttendanceRecord> create(AttendanceRecord attendanceRecord) {
        String sql = """
            INSERT INTO attendance_records (session_id, member_id, attended)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, attendanceRecord.getSessionId());
            stmt.setLong(2, attendanceRecord.getMemberId());
            stmt.setBoolean(3, attendanceRecord.isAttended());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                return Optional.empty();
            }

            // Get the generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);

                    AttendanceRecord created = new AttendanceRecord(
                        id,
                        attendanceRecord.getSessionId(),
                        attendanceRecord.getMemberId(),
                        attendanceRecord.isAttended()
                    );
                    return Optional.of(created);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating attendance record: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean update(AttendanceRecord attendanceRecord) {
        String sql = """
            UPDATE attendance_records
            SET attended = ?
            WHERE id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, attendanceRecord.isAttended());
            stmt.setLong(2, attendanceRecord.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating attendance record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private AttendanceRecord mapResultSetToAttendanceRecord(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long sessionId = rs.getLong("session_id");
        long memberId = rs.getLong("member_id");
        boolean attended = rs.getBoolean("attended");

        return new AttendanceRecord(id, sessionId, memberId, attended);
    }
}

