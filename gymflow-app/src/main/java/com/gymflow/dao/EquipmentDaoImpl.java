package com.gymflow.dao;

import com.gymflow.config.DatabaseConnection;
import com.gymflow.model.Equipment;
import com.gymflow.model.EquipmentStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.gymflow.exception.DataAccessException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of EquipmentDao for database operations on Equipment entities.
 */
public class EquipmentDaoImpl implements EquipmentDao {
    private final DatabaseConnection dbConnection;

    public EquipmentDaoImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Optional<Equipment> findById(long id) throws DataAccessException {
        String sql = """
            SELECT id, name, status, last_service
            FROM equipment
            WHERE id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding equipment by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Equipment> findAll() throws DataAccessException {
        String sql = """
            SELECT id, name, status, last_service
            FROM equipment
            ORDER BY name ASC
            """;

        List<Equipment> equipmentList = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipmentList.add(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all equipment", e);
        }
        return equipmentList;
    }

    @Override
    public List<Equipment> findByStatus(EquipmentStatus status) throws DataAccessException {
        String sql = """
            SELECT id, name, status, last_service
            FROM equipment
            WHERE status = ?
            ORDER BY name ASC
            """;

        List<Equipment> equipmentList = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    equipmentList.add(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding equipment by status", e);
        }
        return equipmentList;
    }

    @Override
    public Optional<Equipment> create(Equipment equipment) throws DataAccessException {
        String sql = """
            INSERT INTO equipment (name, status, last_service)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getStatus().name());
            stmt.setDate(3, equipment.getLastServiceDate() != null ? Date.valueOf(equipment.getLastServiceDate()) : null);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                return Optional.empty();
            }
            // Get the generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    Equipment created = new Equipment(
                        id,
                        equipment.getName(),
                        equipment.getStatus(),
                        equipment.getLastServiceDate()
                    );
                    return Optional.of(created);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating equipment", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean update(Equipment equipment) throws DataAccessException {
        String sql = """
            UPDATE equipment
            SET name = ?, status = ?, last_service = ?
            WHERE id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getStatus().name());
            stmt.setDate(3, equipment.getLastServiceDate() != null ? Date.valueOf(equipment.getLastServiceDate()) : null);
            stmt.setLong(4, equipment.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error updating equipment", e);
        }
    }

    @Override
    public boolean updateStatus(long id, EquipmentStatus status) throws DataAccessException {
        String sql = "UPDATE equipment SET status = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error updating equipment status", e);
        }
    }

    private Equipment mapResultSetToEquipment(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String statusStr = rs.getString("status");
        EquipmentStatus status = EquipmentStatus.fromString(statusStr);
        Date lastServiceDate = rs.getDate("last_service");
        LocalDate lastService = lastServiceDate != null ? lastServiceDate.toLocalDate() : null;

        return new Equipment(id, name, status, lastService);
    }
}

