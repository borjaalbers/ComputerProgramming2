package com.gymflow.service;

import com.gymflow.model.Equipment;
import com.gymflow.model.EquipmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Defines business operations for equipment management.
 */
public interface EquipmentService {
    /**
     * Creates a new equipment entry.
     *
     * @param name the equipment name
     * @param status the initial status
     * @param lastServiceDate when the equipment was last serviced (can be null)
     * @return Optional containing the created Equipment if successful, empty otherwise
     */
    Optional<Equipment> createEquipment(String name, EquipmentStatus status, LocalDate lastServiceDate);

    /**
     * Gets all equipment.
     *
     * @return list of all equipment
     */
    List<Equipment> getAllEquipment();

    /**
     * Gets equipment by status.
     *
     * @param status the equipment status to filter by
     * @return list of equipment with the specified status
     */
    List<Equipment> getEquipmentByStatus(EquipmentStatus status);

    /**
     * Gets equipment by its ID.
     *
     * @param equipmentId the equipment ID
     * @return Optional containing the Equipment if found, empty otherwise
     */
    Optional<Equipment> getEquipmentById(long equipmentId);

    /**
     * Updates equipment information.
     *
     * @param equipmentId the equipment ID
     * @param name the new name (can be null to keep existing)
     * @param status the new status (can be null to keep existing)
     * @param lastServiceDate the new service date (can be null to keep existing)
     * @return true if update was successful, false otherwise
     */
    boolean updateEquipment(long equipmentId, String name, EquipmentStatus status, LocalDate lastServiceDate);

    /**
     * Updates only the status of an equipment.
     *
     * @param equipmentId the equipment ID
     * @param status the new status
     * @return true if update was successful, false otherwise
     */
    boolean updateEquipmentStatus(long equipmentId, EquipmentStatus status);

    /**
     * Marks equipment as needing service.
     *
     * @param equipmentId the equipment ID
     * @return true if update was successful, false otherwise
     */
    boolean markEquipmentForService(long equipmentId);
}

