package com.gymflow.service;

import com.gymflow.dao.AttendanceDao;
import com.gymflow.dao.AttendanceDaoImpl;
import com.gymflow.model.AttendanceRecord;
import com.gymflow.util.CsvUtil;
import com.gymflow.exception.FileOperationException;
import com.gymflow.exception.ValidationException;
import com.gymflow.exception.DataAccessException;

import java.nio.file.Path;
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
    public Optional<AttendanceRecord> markAttendance(long sessionId, long memberId, boolean attended) throws DataAccessException {
        if (sessionId <= 0 || memberId <= 0) return Optional.empty();
        return attendanceDao.markAttendance(sessionId, memberId, attended);
    }

    @Override
    public List<AttendanceRecord> getAttendanceForSession(long sessionId) throws DataAccessException {
        if (sessionId <= 0) return List.of();
        return attendanceDao.findBySessionId(sessionId);
    }

    @Override
    public List<AttendanceRecord> getAttendanceForMember(long memberId) throws DataAccessException {
        if (memberId <= 0) return List.of();
        return attendanceDao.findByMemberId(memberId);
    }

    @Override
    public Optional<AttendanceRecord> getAttendanceRecordById(long recordId) throws DataAccessException {
        if (recordId <= 0) return Optional.empty();
        return attendanceDao.findById(recordId);
    }

    @Override
    public int getAttendanceCount(long sessionId) throws DataAccessException {
        return (int) getAttendanceForSession(sessionId).stream()
                .filter(AttendanceRecord::isAttended)
                .count();
    }

    @Override
    public List<AttendanceRecord> getAllAttendanceRecords() throws DataAccessException {
        return attendanceDao.findAll();
    }

    @Override
    public Optional<AttendanceRecord> registerForClass(long sessionId, long memberId) throws DataAccessException {
        if (sessionId <= 0 || memberId <= 0) return Optional.empty();
        if (isRegisteredForClass(sessionId, memberId)) return Optional.empty();
        return attendanceDao.markAttendance(sessionId, memberId, false);
    }

    @Override
    public boolean unregisterFromClass(long sessionId, long memberId) throws DataAccessException {
        if (sessionId <= 0 || memberId <= 0) return false;
        return attendanceDao.delete(sessionId, memberId);
    }

    @Override
    public boolean isRegisteredForClass(long sessionId, long memberId) throws DataAccessException {
        if (sessionId <= 0 || memberId <= 0) return false;
        return attendanceDao.findBySessionAndMember(sessionId, memberId).isPresent();
    }

    @Override
    public int getRegisteredCount(long sessionId) throws DataAccessException {
        return getAttendanceForSession(sessionId).size();
    }

    /**
     * Exports all attendance records to a CSV file.
     *
     * @param path target file path
     * @throws FileOperationException if export fails
     */
    public void exportAttendanceReport(Path path) throws FileOperationException {
        try {
            List<AttendanceRecord> records = getAllAttendanceRecords();
            CsvUtil.exportAttendanceReport(records, path);
        } catch (DataAccessException e) {
            throw new FileOperationException("Failed to export attendance report due to database error", e);
        } catch (Exception e) {
            throw new FileOperationException("Failed to export attendance report", e);
        }
    }

    /**
     * Imports attendance records from a CSV file and saves them to the database.
     *
     * @param path the CSV file path
     * @throws FileOperationException if file reading fails
     * @throws ValidationException if CSV content is invalid
     */
    public void importAttendanceReport(Path path) throws FileOperationException, ValidationException {
        List<AttendanceRecord> records = CsvUtil.importAttendanceReport(path);
        try {
            for (AttendanceRecord record : records) {
                // Save each record to the database (insert or update)
                attendanceDao.markAttendance(record.getSessionId(), record.getMemberId(), record.isAttended());
            }
        } catch (DataAccessException e) {
            throw new FileOperationException("Failed to import attendance report due to database error", e);
        }
    }
}
