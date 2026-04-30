package com.attendance.dao;

import com.attendance.config.DatabaseConfig;
import com.attendance.model.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> findAll() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, d.name AS dept_name FROM courses c " +
                     "LEFT JOIN departments d ON c.department_id=d.id ORDER BY c.name";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Course> findByDepartment(long deptId) throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, d.name AS dept_name FROM courses c " +
                     "LEFT JOIN departments d ON c.department_id=d.id " +
                     "WHERE c.department_id=? ORDER BY c.name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Course findById(long id) throws SQLException {
        String sql = "SELECT c.*, d.name AS dept_name FROM courses c " +
                     "LEFT JOIN departments d ON c.department_id=d.id WHERE c.id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public long insert(Course c) throws SQLException {
        String sql = "INSERT INTO courses (department_id, name, code, credit_hours, description) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, c.getDepartmentId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getCode());
            ps.setInt(4, c.getCreditHours());
            ps.setString(5, c.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    public void update(Course c) throws SQLException {
        String sql = "UPDATE courses SET department_id=?, name=?, code=?, credit_hours=?, description=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, c.getDepartmentId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getCode());
            ps.setInt(4, c.getCreditHours());
            ps.setString(5, c.getDescription());
            ps.setLong(6, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM courses WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Course map(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getLong("id"));
        c.setDepartmentId(rs.getLong("department_id"));
        c.setName(rs.getString("name"));
        c.setCode(rs.getString("code"));
        c.setCreditHours(rs.getInt("credit_hours"));
        c.setDescription(rs.getString("description"));
        c.setDepartmentName(rs.getString("dept_name"));
        return c;
    }
}
