package com.gymflow.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a piece of gym equipment with status and maintenance tracking.
 * 
 * <p>Equipment can be in various states (AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE)
 * and tracks when it was last serviced for maintenance purposes.</p>
 */
public class Equipment {
    private final long id;
    private String name;
    private EquipmentStatus status;
    private LocalDate lastServiceDate;

    /**
     * Creates a new Equipment instance.
     *
     * @param id the unique identifier
     * @param name the name/description of the equipment
     * @param status the current status of the equipment
     * @param lastServiceDate when the equipment was last serviced (can be null)
     */
    public Equipment(long id, String name, EquipmentStatus status, LocalDate lastServiceDate) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.lastServiceDate = lastServiceDate;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public LocalDate getLastServiceDate() {
        return lastServiceDate;
    }

    public void setLastServiceDate(LocalDate lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
    }

    /**
     * Checks if the equipment is available for use.
     *
     * @return true if status is AVAILABLE
     */
    public boolean isAvailable() {
        return status == EquipmentStatus.AVAILABLE;
    }

    /**
     * Checks if the equipment needs maintenance.
     *
     * @return true if status is MAINTENANCE or OUT_OF_SERVICE
     */
    public boolean needsMaintenance() {
        return status == EquipmentStatus.MAINTENANCE || status == EquipmentStatus.OUT_OF_SERVICE;
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", lastServiceDate=" + lastServiceDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return id == equipment.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

