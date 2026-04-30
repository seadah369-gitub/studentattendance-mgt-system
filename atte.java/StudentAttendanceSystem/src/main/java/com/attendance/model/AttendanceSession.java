package com.attendance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceSession {
    private long id;
    private long classCourseId;
    private LocalDate date;
    private long createdBy;
    private LocalDateTime createdAt;
    private String className;
    private String subjectName;   // display alias for course name
    private String teacherName;

    public AttendanceSession() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClassCourseId() { return classCourseId; }
    public void setClassCourseId(long classCourseId) { this.classCourseId = classCourseId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    /** Returns the course name (labelled "subject" in the UI) */
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
}
