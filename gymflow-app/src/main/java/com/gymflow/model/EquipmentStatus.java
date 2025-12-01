package com.gymflow.model;

/**
 * Enumeration of possible equipment statuses.
 * 
 * <p>These values correspond to the status values stored in the equipment
 * database table.</p>
 */
public enum EquipmentStatus {
    AVAILABLE("Available for use"),
    IN_USE("Currently in use"),
    MAINTENANCE("Under maintenance"),
    OUT_OF_SERVICE("Out of service");

    private final String description;

    EquipmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Converts a string value to an EquipmentStatus enum.
     * Case-insensitive matching.
     *
     * @param status the status string from the database
     * @return the corresponding EquipmentStatus, or AVAILABLE as default
     */
    public static EquipmentStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            return AVAILABLE;
        }
        
        String normalized = status.trim().toUpperCase().replace(" ", "_");
        try {
            return EquipmentStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // If the value doesn't match, default to AVAILABLE
            return AVAILABLE;
        }
    }
}

