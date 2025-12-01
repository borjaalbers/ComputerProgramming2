package com.gymflow.dao;

import com.gymflow.model.Equipment;
import com.gymflow.model.EquipmentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for Equipment entities.
 */
public interface EquipmentDao {
    /**
     * Finds equipment by its ID.
     *
     * @param id the equipment ID
     * @return Optional containing the Equipment if found, empty otherwise
     */
    Optional<Equipment> findById(long id);

    /**
     * Finds all equipment in the database.
     *
     * @return list of all equipment
     */
    List<Equipment> findAll();

    /**
     * Finds equipment by status.
     *
     * @param status the equipment status to filter by
     * @return list of equipment with the specified status
     */
    List<Equipment> findByStatus(EquipmentStatus status);

    /**
     * Creates a new equipment entry in the database.
     *
     * @param equipment the equipment to create (id will be generated)
     * @return Optional containing the created Equipment with generated ID, empty if creation fails
     */
    Optional<Equipment> create(Equipment equipment);

    /**
     * Updates an existing equipment entry in the database.
     *
     * @param equipment the equipment to update (must have valid id)
     * @return true if update was successful, false otherwise
     */
    boolean update(Equipment equipment);

    /**
     * Updates only the status of an equipment entry.
     *
     * @param id the equipment ID
     * @param status the new status
     * @return true if update was successful, false otherwise
     */
    boolean updateStatus(long id, EquipmentStatus status);
}

