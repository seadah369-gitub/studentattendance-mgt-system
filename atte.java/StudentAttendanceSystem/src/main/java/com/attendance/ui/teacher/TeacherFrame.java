package com.attendance.ui.teacher;

import com.attendance.dao.TeacherDAO;
import com.attendance.model.Teacher;
import com.attendance.model.User;
import com.attendance.service.AuthService;
import com.attendance.ui.auth.LoginFrame;
import com.attendance.ui.common.SidebarPanel;
import com.attendance.util.UIUtil;

import javax.swing.*;
import java.awt.*;

public class TeacherFrame extends JFrame {

    private final JPanel contentPanel;
    private final CardLayout cardLayout;

    public TeacherFrame(User user) {
        setTitle("Teacher Dashboard — " + user.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 580));

        Teacher teacher = null;
        try { teacher = new TeacherDAO().findByUserId(user.getId()); } catch (Exception ignored) {}
        final Teacher finalTeacher = teacher;

        SidebarPanel sidebar = new SidebarPanel("Attendance System", user.getFullName(), "TEACHER");
        sidebar.addSectionLabel("Main");
        JButton dashBtn = sidebar.addMenu("📊", "Dashboard",       "dashboard");
        sidebar.addSectionLabel("Attendance");
        sidebar.addMenu("✅", "Mark Attendance",  "mark");
        sidebar.addMenu("✏", "Edit Attendance",   "edit");
        sidebar.addSectionLabel("Reports");
        sidebar.addMenu("📈", "Class Reports",    "reports");
        sidebar.addSectionLabel("Account");
        sidebar.addMenu("🔑", "Change Password", "changepw");
        sidebar.addMenu("🚪", "Logout",           "logout");

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtil.BG);

        if (finalTeacher != null) {
            contentPanel.add(new TeacherDashboardPanel(finalTeacher), "dashboard");
            contentPanel.add(new MarkAttendancePanel(finalTeacher),   "mark");
            contentPanel.add(new EditAttendancePanel(finalTeacher),   "edit");
            contentPanel.add(new TeacherReportPanel(finalTeacher),    "reports");
            contentPanel.add(new com.attendance.ui.common.ChangePasswordPanel(user), "changepw");
        } else {
            JLabel err = new JLabel("Teacher profile not found. Contact admin.", SwingConstants.CENTER);
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
