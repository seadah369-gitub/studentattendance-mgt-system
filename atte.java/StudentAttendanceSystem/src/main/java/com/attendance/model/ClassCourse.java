package com.attendance.model;

public class ClassCourse {
    private long id;
    private long classId;
    private long courseId;
    private long teacherId;
    private String className;
    private String courseName;
    private String courseCode;
    private String teacherName;

    public ClassCourse() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getClassId() { return classId; }
    public void setClassId(long classId) { this.classId = classId; }
    public long getCourseId() { return courseId; }
    public void setCourseId(long courseId) { this.courseId = courseId; }
    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    @Override
    public String toString() {
        return className + "  —  " + courseName
            + (courseCode != null ? " (" + courseCode + ")" : "");
    }
}
