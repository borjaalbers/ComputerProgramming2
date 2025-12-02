package com.gymflow.dao;

import com.gymflow.model.AttendanceRecord;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for AttendanceRecord entities.
 */
public interface AttendanceDao {
    /**
     * Finds an attendance record by its ID.
     *
     * @param id the attendance record ID
     * @return Optional containing the AttendanceRecord if found, empty otherwise
     */
    Optional<AttendanceRecord> findById(long id);

    /**
     * Finds all attendance records for a specific class session.
     *
     * @param sessionId the class session ID
     * @return list of attendance records for the session
     */
    List<AttendanceRecord> findBySessionId(long sessionId);

    /**
     * Finds all attendance records for a specific member.
     *
     * @param memberId the member ID
     * @return list of attendance records for the member
     */
    List<AttendanceRecord> findByMemberId(long memberId);

    /**
     * Finds all attendance records in the system.
     *
     * @return list of all attendance records
     */
    List<AttendanceRecord> findAll();

    /**
     * Marks attendance for a member in a class session.
     *
     * @param sessionId the class session ID
     * @param memberId  the member ID
     * @param attended  whether the member attended
     * @return Optional containing the AttendanceRecord if successful, empty otherwise
     */
    Optional<AttendanceRecord> markAttendance(long sessionId, long memberId, boolean attended);

    /**
     * Creates a new attendance record in the database.
     *
     * @param attendanceRecord the attendance record to create (id will be generated)
     * @return Optional containing the created AttendanceRecord with generated ID, empty if creation fails
     */
    Optional<AttendanceRecord> create(AttendanceRecord attendanceRecord);

    /**
     * Updates an existing attendance record in the database.
     *
     * @param attendanceRecord the attendance record to update (must have valid id)
     * @return true if update was successful, false otherwise
     */
    boolean update(AttendanceRecord attendanceRecord);

    /**
     * Deletes an attendance record from the database.
     *
     * @param sessionId the class session ID
     * @param memberId  the member ID
     * @return true if deletion was successful, false otherwise
     */
    boolean delete(long sessionId, long memberId);

    /**
     * Finds an attendance record by session ID and member ID.
     *
     * @param sessionId the class session ID
     * @param memberId  the member ID
     * @return Optional containing the AttendanceRecord if found, empty otherwise
     */
    Optional<AttendanceRecord> findBySessionAndMember(long sessionId, long memberId);
}

