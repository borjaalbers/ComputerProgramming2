package com.gymflow.service;

import com.gymflow.dao.AttendanceDao;
import com.gymflow.dao.AttendanceDaoImpl;
import com.gymflow.model.AttendanceRecord;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of AttendanceService for attendance tracking business logic.
 */
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceDao attendanceDao;

    public AttendanceServiceImpl() {
        this.attendanceDao = new AttendanceDaoImpl();
    }

    @Override
    public Optional<AttendanceRecord> markAttendance(long sessionId, long memberId, boolean attended) {
        // Validation
        if (sessionId <= 0 || memberId <= 0) {
            System.err.println("Invalid session or member ID");
            return Optional.empty();
        }

        // Use DAO's markAttendance which handles create/update automatically
        Optional<AttendanceRecord> result = attendanceDao.markAttendance(sessionId, memberId, attended);
        
        if (result.isPresent()) {
            System.out.println("Attendance marked: Member " + memberId + " - Session " + sessionId + " - Attended: " + attended);
        } else {
            System.err.println("Failed to mark attendance for member " + memberId + " in session " + sessionId);
        }

        return result;
    }

    @Override
    public List<AttendanceRecord> getAttendanceForSession(long sessionId) {
        if (sessionId <= 0) {
            System.err.println("Invalid session ID");
            return List.of();
        }

        return attendanceDao.findBySessionId(sessionId);
    }

    @Override
    public List<AttendanceRecord> getAttendanceForMember(long memberId) {
        if (memberId <= 0) {
            System.err.println("Invalid member ID");
            return List.of();
        }

        return attendanceDao.findByMemberId(memberId);
    }

    @Override
    public Optional<AttendanceRecord> getAttendanceRecordById(long recordId) {
        if (recordId <= 0) {
            return Optional.empty();
        }

        return attendanceDao.findById(recordId);
    }

    @Override
    public int getAttendanceCount(long sessionId) {
        List<AttendanceRecord> records = getAttendanceForSession(sessionId);
        
        // Count only records where attended is true
        return (int) records.stream()
                .filter(AttendanceRecord::isAttended)
                .count();
    }

    @Override
    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceDao.findAll();
    }
}

