package com.attendance.model;

public class Course {
    private long id;
    private long departmentId;
    private String name;
    private String code;
    private int creditHours;
    private String description;
    private String departmentName;

    public Course() {}
    public Course(long id, String name) { this.id = id; this.name = name; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getDepartmentId() { return departmentId; }
    public void setDepartmentId(long departmentId) { this.departmentId = departmentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getCreditHours() { return creditHours; }
    public void setCreditHours(int creditHours) { this.creditHours = creditHours; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    @Override
    public String toString() {
        return code != null && !code.isEmpty() ? name + " (" + code + ")" : name;
    }
}
