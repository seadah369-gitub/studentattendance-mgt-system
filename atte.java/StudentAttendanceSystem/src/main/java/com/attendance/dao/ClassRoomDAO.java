package com.attendance.dao;

import com.attendance.config.DatabaseConfig;
import com.attendance.model.ClassRoom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassRoomDAO {

    public List<ClassRoom> findAll() throws SQLException {
        List<ClassRoom> list = new ArrayList<>();
        String sql = "SELECT cl.*, c.name AS course_name FROM classes cl LEFT JOIN courses c ON cl.course_id=c.id ORDER BY cl.name";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public ClassRoom findById(long id) throws SQLException {
        String sql = "SELECT cl.*, c.name AS course_name FROM classes cl LEFT JOIN courses c ON cl.course_id=c.id WHERE cl.id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public long insert(ClassRoom cl) throws SQLException {
        String sql = "INSERT INTO classes (course_id, name, year, semester) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, cl.getCourseId());
            ps.setString(2, cl.getName());
            ps.setInt(3, cl.getYear());
            ps.setInt(4, cl.getSemester());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    public void update(ClassRoom cl) throws SQLException {
        String sql = "UPDATE classes SET course_id=?, name=?, year=?, semester=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cl.getCourseId());
            ps.setString(2, cl.getName());
            ps.setInt(3, cl.getYear());
            ps.setInt(4, cl.getSemester());
            ps.setLong(5, cl.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM classes WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM classes";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private ClassRoom map(ResultSet rs) throws SQLException {
        ClassRoom cl = new ClassRoom();
        cl.setId(rs.getLong("id"));
        cl.setCourseId(rs.getLong("course_id"));
        cl.setName(rs.getString("name"));
        cl.setYear(rs.getInt("year"));
        cl.setSemester(rs.getInt("semester"));
        cl.setCourseName(rs.getString("course_name"));
        return cl;
    }
}
