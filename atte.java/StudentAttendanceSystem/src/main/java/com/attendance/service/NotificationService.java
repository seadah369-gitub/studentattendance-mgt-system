package com.attendance.service;

import com.attendance.config.DatabaseConfig;
import com.attendance.dao.UserDAO;
import com.attendance.model.Notification;
import com.attendance.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private final SystemConfigService configSvc = new SystemConfigService();
    private final EmailService        emailSvc  = new EmailService(configSvc);
    private final UserDAO             userDAO   = new UserDAO();

    /**
     * Saves a notification to the database AND sends an email if email is enabled.
     * Email failures are logged to stderr but never crash the caller.
     *
     * @param userId  the user to notify
     * @param message notification text
     * @param type    notification type (e.g. "LOW_ATTENDANCE")
     */
    public void send(long userId, String message, String type) {
        // 1. Always save to DB
        String sql = "INSERT INTO notifications (user_id, message, type) VALUES (?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, message);
            ps.setString(3, type);
            ps.executeUpdate();
        } catch (SQLException ignored) {}

        // 2. Send email if enabled (non-blocking — runs in background)
        if (emailSvc.isEnabled()) {
            Thread emailThread = new Thread(() -> {
                try {
                    User user = userDAO.findById(userId);
                    if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                        String subject = buildSubject(type);
                        emailSvc.send(user.getEmail(), subject, message);
                    }
                } catch (Exception ex) {
                    System.err.println("Email notification failed for userId=" + userId
                        + ": " + ex.getMessage());
                }
            }, "email-notif-" + userId);
            emailThread.setDaemon(true);
            emailThread.start();
        }
    }

    public List<Notification> getUnread(long userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id=? AND is_read=FALSE ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Notification> getAll(long userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void markAllRead(long userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read=TRUE WHERE user_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    public int countUnread(long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=FALSE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ---- Helpers ----

    private String buildSubject(String type) {
        return switch (type) {
            case "LOW_ATTENDANCE" -> "⚠ Low Attendance Warning — Action Required";
            default               -> "Attendance System Notification";
        };
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setRead(rs.getBoolean("is_read"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
        return n;
    }
}
