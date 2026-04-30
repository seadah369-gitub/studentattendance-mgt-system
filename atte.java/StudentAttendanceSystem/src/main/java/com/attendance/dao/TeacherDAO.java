package com.attendance.dao;

import com.attendance.config.DatabaseConfig;
import com.attendance.model.Teacher;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO {

    public long insert(Teacher t) throws SQLException {
        String sql = "INSERT INTO teachers (user_id, employee_id, specialization) VALUES (?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, t.getUserId());
            ps.setString(2, t.getEmployeeId());
            ps.setString(3, t.getSpecialization());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    public void update(Teacher t) throws SQLException {
        String sql = "UPDATE teachers SET employee_id=?, specialization=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getEmployeeId());
            ps.setString(2, t.getSpecialization());
            ps.setLong(3, t.getId());
            ps.executeUpdate();
        }
    }

    public Teacher findByUserId(long userId) throws SQLException {
        String sql = "SELECT t.*, u.first_name, u.last_name, u.email, u.phone, u.status " +
                     "FROM teachers t JOIN users u ON t.user_id = u.id WHERE t.user_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapTeacher(rs);
            }
        }
        return null;
    }

    public Teacher findById(long id) throws SQLException {
        String sql = "SELECT t.*, u.first_name, u.last_name, u.email, u.phone, u.status " +
                     "FROM teachers t JOIN users u ON t.user_id = u.id WHERE t.id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapTeacher(rs);
            }
        }
        return null;
    }

    public List<Teacher> findAll() throws SQLException {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT t.*, u.first_name, u.last_name, u.email, u.phone, u.status " +
                     "FROM teachers t JOIN users u ON t.user_id = u.id ORDER BY u.first_name";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapTeacher(rs));
        }
        return list;
    }

    private Teacher mapTeacher(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getLong("id"));
        t.setUserId(rs.getLong("user_id"));
        t.setEmployeeId(rs.getString("employee_id"));
        t.setSpecialization(rs.getString("specialization"));
        t.setFirstName(rs.getString("first_name"));
        t.setLastName(rs.getString("last_name"));
        t.setEmail(rs.getString("email"));
        t.setPhone(rs.getString("phone"));
        t.setStatus(rs.getBoolean("status"));
        return t;
    }
}
