package com.gymflow.model;

import java.util.Objects;

/**
 * Represents a member's attendance record for a specific class session.
 * 
 * <p>This class tracks whether a member attended a scheduled class session.
 * It links a member to a class session and records their attendance status.</p>
 */
public class AttendanceRecord {
    private final long id;
    private final long sessionId;
    private final long memberId;
    private boolean attended;

    /**
     * Creates a new AttendanceRecord.
     *
     * @param id the unique identifier
     * @param sessionId the ID of the class session
     * @param memberId the ID of the member
     * @param attended whether the member attended the session
     */
    public AttendanceRecord(long id, long sessionId, long memberId, boolean attended) {
        this.id = id;
        this.sessionId = sessionId;
        this.memberId = memberId;
        this.attended = attended;
    }

    public long getId() {
        return id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public long getMemberId() {
        return memberId;
    }

    public boolean isAttended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    /**
     * Marks the member as having attended the session.
     */
    public void markAsAttended() {
        this.attended = true;
    }

    /**
     * Marks the member as not having attended the session.
     */
    public void markAsAbsent() {
        this.attended = false;
    }

    @Override
    public String toString() {
        return "AttendanceRecord{" +
                "id=" + id +
                ", sessionId=" + sessionId +
                ", memberId=" + memberId +
                ", attended=" + attended +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceRecord that = (AttendanceRecord) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

