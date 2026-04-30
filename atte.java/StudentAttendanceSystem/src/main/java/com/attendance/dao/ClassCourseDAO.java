package com.attendance.dao;

import com.attendance.config.DatabaseConfig;
import com.attendance.model.ClassCourse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassCourseDAO {

    private static final String BASE_SQL =
        "SELECT cc.*, cl.name AS class_name, c.name AS course_name, c.code AS course_code, " +
        "CONCAT(u.first_name,' ',u.last_name) AS teacher_name " +
        "FROM class_courses cc " +
        "JOIN classes cl ON cc.class_id=cl.id " +
        "JOIN courses c ON cc.course_id=c.id " +
        "LEFT JOIN teachers t ON cc.teacher_id=t.id " +
        "LEFT JOIN users u ON t.user_id=u.id ";

    public List<ClassCourse> findAll() throws SQLException {
        List<ClassCourse> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(BASE_SQL + "ORDER BY cl.name, c.name")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<ClassCourse> findByTeacher(long teacherId) throws SQLException {
        List<ClassCourse> list = new ArrayList<>();
        String sql = BASE_SQL + "WHERE cc.teacher_id=? ORDER BY cl.name, c.name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<ClassCourse> findByClass(long classId) throws SQLException {
        List<ClassCourse> list = new ArrayList<>();
        String sql = BASE_SQL + "WHERE cc.class_id=? ORDER BY c.name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public ClassCourse findById(long id) throws SQLException {
        String sql = BASE_SQL + "WHERE cc.id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public long insert(ClassCourse cc) throws SQLException {
        // If same class+course already exists, just update the teacher
        String sql = "INSERT INTO class_courses (class_id, course_id, teacher_id) VALUES (?,?,?) " +
                     "ON DUPLICATE KEY UPDATE teacher_id=VALUES(teacher_id), id=LAST_INSERT_ID(id)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, cc.getClassId());
            ps.setLong(2, cc.getCourseId());
            if (cc.getTeacherId() > 0) ps.setLong(3, cc.getTeacherId());
            else ps.setNull(3, Types.BIGINT);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    public void update(ClassCourse cc) throws SQLException {
        String sql = "UPDATE class_courses SET class_id=?, course_id=?, teacher_id=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cc.getClassId());
            ps.setLong(2, cc.getCourseId());
            if (cc.getTeacherId() > 0) ps.setLong(3, cc.getTeacherId());
            else ps.setNull(3, Types.BIGINT);
            ps.setLong(4, cc.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM class_courses WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private ClassCourse map(ResultSet rs) throws SQLException {
        ClassCourse cc = new ClassCourse();
        cc.setId(rs.getLong("id"));
        cc.setClassId(rs.getLong("class_id"));
        cc.setCourseId(rs.getLong("course_id"));
        cc.setTeacherId(rs.getLong("teacher_id"));
        cc.setClassName(rs.getString("class_name"));
        cc.setCourseName(rs.getString("course_name"));
        cc.setCourseCode(rs.getString("course_code"));
        cc.setTeacherName(rs.getString("teacher_name"));
        return cc;
    }
}
