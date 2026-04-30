package com.attendance.ui.student;

import com.attendance.dao.StudentDAO;
import com.attendance.model.Student;
import com.attendance.model.User;
import com.attendance.service.AuthService;
import com.attendance.ui.auth.LoginFrame;
import com.attendance.ui.common.SidebarPanel;
import com.attendance.util.UIUtil;

import javax.swing.*;
import java.awt.*;

public class StudentFrame extends JFrame {

    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    public StudentFrame(User user) {
        setTitle("Student Portal — " + user.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 560));

        Student student = null;
        try { student = new StudentDAO().findByUserId(user.getId()); } catch (Exception ignored) {}
        final Student finalStudent = student;

        SidebarPanel sidebar = new SidebarPanel("Attendance System", user.getFullName(), "STUDENT");
        sidebar.addSectionLabel("Main");
        JButton dashBtn = sidebar.addMenu("📊", "Dashboard",        "dashboard");
        sidebar.addSectionLabel("Attendance");
        sidebar.addMenu("📅", "Monthly Calendar",  "calendar");
        sidebar.addMenu("📖", "Subject Breakdown", "subjects");
        sidebar.addMenu("📋", "Full History",       "history");
        sidebar.addSectionLabel("Account");
        sidebar.addMenu("👤", "Edit Profile",    "profile");
        sidebar.addMenu("🔑", "Change Password", "changepw");
        sidebar.addMenu("🚪", "Logout",             "logout");

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtil.BG);

        if (finalStudent != null) {
            contentPanel.add(new StudentDashboardPanel(finalStudent),   "dashboard");
            contentPanel.add(new AttendanceCalendarPanel(finalStudent), "calendar");
            contentPanel.add(new SubjectBreakdownPanel(finalStudent),   "subjects");
            contentPanel.add(new AttendanceHistoryPanel(finalStudent),  "history");
            contentPanel.add(new EditProfilePanel(finalStudent, user),  "profile");
            contentPanel.add(new com.attendance.ui.common.ChangePasswordPanel(user), "changepw");
        } else {
            JLabel err = new JLabel("Student profile not found. Contact admin.", SwingConstants.CENTER);
            err.setFont(UIUtil.FONT_HEADER);
            contentPanel.add(err, "dashboard");
            contentPanel.add(new com.attendance.ui.common.ChangePasswordPanel(user), "changepw");
        }

        sidebar.setMenuListener(id -> {
            if ("logout".equals(id)) {
                if (UIUtil.confirm(this, "Logout?")) {
                    AuthService.getInstance().logout();
                    dispose();
                    SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
                }
            } else {
                cardLayout.show(contentPanel, id);
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar, BorderLayout.WEST);
        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);

        sidebar.setActive(dashBtn);
        cardLayout.show(contentPanel, "dashboard");
    }
}
