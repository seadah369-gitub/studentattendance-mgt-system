package com.attendance.dao;

import com.attendance.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    public void enroll(long studentId, long classId) throws SQLException {
        String sql = "INSERT IGNORE INTO enrollments (student_id, class_id) VALUES (?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            ps.executeUpdate();
        }
    }

    public void unenroll(long studentId, long classId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE student_id=? AND class_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            ps.executeUpdate();
        }
    }

    public boolean isEnrolled(long studentId, long classId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id=? AND class_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Long> getClassIdsByStudent(long studentId) throws SQLException {
        List<Long> ids = new ArrayList<>();
        String sql = "SELECT class_id FROM enrollments WHERE student_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong("class_id"));
            }
        }
        return ids;
    }
}
