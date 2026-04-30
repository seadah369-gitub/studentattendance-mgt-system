package com.attendance.service;

import com.attendance.config.DatabaseConfig;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SystemConfigService {

    public String get(String key, String defaultValue) {
        String sql = "SELECT config_value FROM system_config WHERE config_key=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("config_value");
            }
        } catch (SQLException ignored) {}
        return defaultValue;
    }

    public void set(String key, String value) throws SQLException {
        String sql = "INSERT INTO system_config (config_key, config_value) VALUES (?,?) " +
                     "ON DUPLICATE KEY UPDATE config_value=VALUES(config_value)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    public Map<String, String> getAll() throws SQLException {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT config_key, config_value FROM system_config";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString("config_key"), rs.getString("config_value"));
        }
        return map;
    }

    public double getAttendanceThreshold() {
        return Double.parseDouble(get("attendance_threshold", "75"));
    }

    public int getCurrentSemester() {
        return Integer.parseInt(get("current_semester", "1"));
    }

    public String getAcademicYear() {
        return get("academic_year", "2025-2026");
    }
}
