package com.gymflow.service;

import com.gymflow.model.AttendanceRecord;

import java.util.List;
import java.util.Optional;

/**
 * Defines business operations for attendance tracking.
 */
public interface AttendanceService {
    /**
     * Marks attendance for a member in a class session.
     *
     * @param sessionId the class session ID
     * @param memberId the member ID
     * @param attended whether the member attended
     * @return Optional containing the AttendanceRecord if successful, empty otherwise
     */
    Optional<AttendanceRecord> markAttendance(long sessionId, long memberId, boolean attended);

    /**
     * Gets all attendance records for a specific class session.
     *
     * @param sessionId the class session ID
     * @return list of attendance records for the session
     */
    List<AttendanceRecord> getAttendanceForSession(long sessionId);

    /**
     * Gets all attendance records for a specific member.
     *
     * @param memberId the member ID
     * @return list of attendance records for the member
     */
    List<AttendanceRecord> getAttendanceForMember(long memberId);

    /**
     * Gets an attendance record by its ID.
     *
     * @param recordId the attendance record ID
     * @return Optional containing the AttendanceRecord if found, empty otherwise
     */
    Optional<AttendanceRecord> getAttendanceRecordById(long recordId);

    /**
     * Gets the attendance count for a class session.
     *
     * @param sessionId the class session ID
     * @return number of members who attended
     */
    int getAttendanceCount(long sessionId);

    /**
     * Gets all attendance records in the system.
     *
     * @return list of all attendance records
     */
    List<AttendanceRecord> getAllAttendanceRecords();
}

