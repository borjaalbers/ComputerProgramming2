package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.AttendanceRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.gymflow.exception.DataAccessException;
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
    public Optional<AttendanceRecord> findById(long id) throws DataAccessException {
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
            throw new DataAccessException("Error finding attendance record by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<AttendanceRecord> findBySessionId(long sessionId) throws DataAccessException {
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
            throw new DataAccessException("Error finding attendance records by session ID", e);
        }
        return records;
    }

    @Override
    public List<AttendanceRecord> findByMemberId(long memberId) throws DataAccessException {
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
            throw new DataAccessException("Error finding attendance records by member ID", e);
        }
        return records;
    }

    @Override
    public List<AttendanceRecord> findAll() throws DataAccessException {
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
            throw new DataAccessException("Error finding all attendance records", e);
        }
        return records;
    }

    @Override
    public Optional<AttendanceRecord> markAttendance(long sessionId, long memberId, boolean attended) throws DataAccessException {
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
            throw new DataAccessException("Error marking attendance", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<AttendanceRecord> create(AttendanceRecord attendanceRecord) throws DataAccessException {
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
            throw new DataAccessException("Error creating attendance record", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean update(AttendanceRecord attendanceRecord) throws DataAccessException {
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
            throw new DataAccessException("Error updating attendance record", e);
        }
    }

    @Override
    public boolean delete(long sessionId, long memberId) throws DataAccessException {
        String sql = """
            DELETE FROM attendance_records
            WHERE session_id = ? AND member_id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sessionId);
            stmt.setLong(2, memberId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting attendance record", e);
        }
    }

    @Override
    public Optional<AttendanceRecord> findBySessionAndMember(long sessionId, long memberId) throws DataAccessException {
        String sql = """
            SELECT id, session_id, member_id, attended
            FROM attendance_records
            WHERE session_id = ? AND member_id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sessionId);
            stmt.setLong(2, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAttendanceRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding attendance record by session and member", e);
        }
        return Optional.empty();
    }

    private AttendanceRecord mapResultSetToAttendanceRecord(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long sessionId = rs.getLong("session_id");
        long memberId = rs.getLong("member_id");
        boolean attended = rs.getBoolean("attended");

        return new AttendanceRecord(id, sessionId, memberId, attended);
    }
}

