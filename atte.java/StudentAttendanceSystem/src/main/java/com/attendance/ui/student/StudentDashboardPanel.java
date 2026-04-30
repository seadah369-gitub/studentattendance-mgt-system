package com.attendance.ui.student;

import com.attendance.dao.AttendanceDAO;
import com.attendance.model.Notification;
import com.attendance.model.Student;
import com.attendance.service.NotificationService;
import com.attendance.service.SystemConfigService;
import com.attendance.util.AvatarUtil;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StudentDashboardPanel extends JPanel {

    private final Student student;
    private final AttendanceDAO attendanceDAO   = new AttendanceDAO();
    private final SystemConfigService configSvc = new SystemConfigService();
    private final NotificationService notifSvc  = new NotificationService();

    public StudentDashboardPanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout());
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = UIUtil.headerLabel("My Attendance Dashboard");
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Overall attendance card
        JPanel overallCard = UIUtil.card("Overall Attendance");
        overallCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        overallCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel pctLabel = new JLabel("Loading...");
        pctLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        pctLabel.setForeground(UIUtil.PRIMARY);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(0, 22));
        progressBar.setFont(UIUtil.FONT_BOLD);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(UIUtil.FONT_BODY);

        JPanel pctPanel = new JPanel(new BorderLayout(0, 6));
        pctPanel.setOpaque(false);
        pctPanel.add(pctLabel, BorderLayout.NORTH);
        pctPanel.add(progressBar, BorderLayout.CENTER);
        pctPanel.add(statusLabel, BorderLayout.SOUTH);
        overallCard.add(pctPanel, BorderLayout.CENTER);

        center.add(overallCard);
        center.add(Box.createVerticalStrut(16));

        // Profile card — avatar left, info right
        JPanel infoCard = UIUtil.card("My Profile");
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar (clickable to change)
        String initials = (student.getFirstName() != null ? student.getFirstName().substring(0, 1) : "")
                        + (student.getLastName()  != null ? student.getLastName().substring(0, 1)  : "");
        JLabel avatarLabel = AvatarUtil.makeAvatarLabel(
            student.getId(), 80, initials, UIUtil.PRIMARY, null);
        avatarLabel.setBorder(new EmptyBorder(0, 0, 0, 16));

        JPanel infoGrid = new JPanel(new GridLayout(0, 2, 12, 6));
        infoGrid.setOpaque(false);
        infoGrid.add(UIUtil.boldLabel("Name:"));
        infoGrid.add(UIUtil.label(student.getFullName()));
        infoGrid.add(UIUtil.boldLabel("Student Code:"));
        infoGrid.add(UIUtil.label(student.getStudentCode()));
        infoGrid.add(UIUtil.boldLabel("Email:"));
        infoGrid.add(UIUtil.label(student.getEmail() != null ? student.getEmail() : "—"));
        infoGrid.add(UIUtil.boldLabel("Gender:"));
        infoGrid.add(UIUtil.label(student.getGender() != null ? student.getGender() : "—"));

        JPanel infoContent = new JPanel(new BorderLayout(0, 0));
        infoContent.setOpaque(false);
        infoContent.add(avatarLabel, BorderLayout.WEST);
        infoContent.add(infoGrid,    BorderLayout.CENTER);
        infoCard.add(infoContent, BorderLayout.CENTER);
        center.add(infoCard);
        center.add(Box.createVerticalStrut(16));

        // Notifications card
        JPanel notifCard = UIUtil.card("Notifications");
        notifCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        notifCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea notifArea = new JTextArea();
        notifArea.setFont(UIUtil.FONT_BODY);
        notifArea.setEditable(false);
        notifArea.setBackground(UIUtil.CARD_BG);
        notifCard.add(new JScrollPane(notifArea), BorderLayout.CENTER);
        center.add(notifCard);

        add(center, BorderLayout.CENTER);

        // Load attendance %
        SwingWorker<double[], Void> w = new SwingWorker<double[], Void>() {
            @Override protected double[] doInBackground() throws Exception {
                double pct = attendanceDAO.getAttendancePercentage(student.getId());
                double threshold = configSvc.getAttendanceThreshold();
                return new double[]{pct, threshold};
            }
            @Override protected void done() {
                try {
                    double[] data = get();
                    double pct = data[0];
                    double threshold = data[1];
                    pctLabel.setText(String.format("%.1f%%", pct));
                    progressBar.setValue((int) pct);
                    progressBar.setString(String.format("%.1f%%", pct));
                    if (pct < threshold) {
                        progressBar.setForeground(UIUtil.DANGER);
                        statusLabel.setText("⚠ Warning: Below " + (int) threshold + "% threshold!");
                        statusLabel.setForeground(UIUtil.DANGER);
                    } else {
                        progressBar.setForeground(UIUtil.SUCCESS);
                        statusLabel.setText("✓ Attendance is satisfactory.");
                        statusLabel.setForeground(UIUtil.SUCCESS);
                    }
                } catch (Exception ignored) {}
            }
        };
        w.execute();

        // Load notifications and mark them read
        SwingWorker<List<Notification>, Void> nw = new SwingWorker<List<Notification>, Void>() {
            @Override protected List<Notification> doInBackground() throws Exception {
                List<Notification> list = notifSvc.getAll(student.getUserId());
                notifSvc.markAllRead(student.getUserId());
                return list;
            }
            @Override protected void done() {
                try {
                    List<Notification> notifs = get();
                    if (notifs.isEmpty()) {
                        notifArea.setText("No notifications.");
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (Notification n : notifs)
                            sb.append(n.isRead() ? "  " : "● ").append(n.getMessage()).append("\n");
                        notifArea.setText(sb.toString());
                    }
                } catch (Exception ignored) {}
            }
        };
        nw.execute();
    }
}
