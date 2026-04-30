package com.attendance.dao;

import com.attendance.config.DatabaseConfig;
import com.attendance.model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public long insert(Student s) throws SQLException {
        String sql = "INSERT INTO students (user_id, student_code, date_of_birth, gender) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, s.getUserId());
            ps.setString(2, s.getStudentCode());
            ps.setDate(3, s.getDateOfBirth() != null ? Date.valueOf(s.getDateOfBirth()) : null);
            ps.setString(4, s.getGender());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    public void update(Student s) throws SQLException {
        String sql = "UPDATE students SET student_code=?, date_of_birth=?, gender=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getStudentCode());
            ps.setDate(2, s.getDateOfBirth() != null ? Date.valueOf(s.getDateOfBirth()) : null);
            ps.setString(3, s.getGender());
            ps.setLong(4, s.getId());
            ps.executeUpdate();
        }
    }

    public Student findByUserId(long userId) throws SQLException {
        String sql = "SELECT s.*, u.first_name, u.last_name, u.email, u.phone, u.status " +
                     "FROM students s JOIN users u ON s.user_id = u.id WHERE s.user_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapStudent(rs);
            }
        }
        return null;
    }

    public Student findById(long id) throws SQLException {
        String sql = "SELECT s.*, u.first_name, u.last_name, u.email, u.phone, u.status " +
                     "FROM students s JOIN users u ON s.user_id = u.id WHERE s.id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapStudent(rs);
            }
        }
        return null;
    }

    public List<Student> findAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, u.first_name, u.last_name, u.email, u.phone, u.status " +
                     "FROM students s JOIN users u ON s.user_id = u.id ORDER BY u.first_name";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapStudent(rs));
        }
        return list;
    }

    public List<Student> findByClass(long classId) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, u.first_name, u.last_name, u.email, u.phone, u.status " +
                     "FROM students s JOIN users u ON s.user_id = u.id " +
                     "JOIN enrollments e ON e.student_id = s.id " +
                     "WHERE e.class_id=? ORDER BY u.first_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapStudent(rs));
            }
        }
        return list;
    }

    public List<Student> search(String keyword) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, u.first_name, u.last_name, u.email, u.phone, u.status " +
                     "FROM students s JOIN users u ON s.user_id = u.id " +
                     "WHERE u.first_name LIKE ? OR u.last_name LIKE ? OR s.student_code LIKE ? " +
                     "ORDER BY u.first_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapStudent(rs));
            }
        }
        return list;
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getLong("id"));
        s.setUserId(rs.getLong("user_id"));
        s.setStudentCode(rs.getString("student_code"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) s.setDateOfBirth(dob.toLocalDate());
        s.setGender(rs.getString("gender"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setStatus(rs.getBoolean("status"));
        return s;
    }
}
