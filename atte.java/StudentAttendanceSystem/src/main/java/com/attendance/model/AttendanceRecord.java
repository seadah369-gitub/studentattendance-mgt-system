package com.attendance.model;

import java.time.LocalDate;

public class AttendanceRecord {
    private long id;
    private long sessionId;
    private long studentId;
    private String status; // PRESENT, ABSENT, LATE
    private String remark;
    private String studentName;
    private String studentCode;
    private LocalDate sessionDate;
    private String subjectName;
    private String className;

    public AttendanceRecord() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }
    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
