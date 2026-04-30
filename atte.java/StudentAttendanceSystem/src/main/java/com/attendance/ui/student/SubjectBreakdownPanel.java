package com.attendance.ui.student;

import com.attendance.dao.AttendanceDAO;
import com.attendance.model.Student;
import com.attendance.service.SystemConfigService;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SubjectBreakdownPanel extends JPanel {

    private final Student student;
    private final AttendanceDAO attendanceDAO   = new AttendanceDAO();
    private final SystemConfigService configSvc = new SystemConfigService();

    public SubjectBreakdownPanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout());
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = UIUtil.headerLabel("Subject-wise Attendance");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Course", "Code", "Total", "Present", "Absent", "Late", "Percentage %", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIUtil.styleTable(table);
        table.setRowHeight(34);

        double threshold = configSvc.getAttendanceThreshold();

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel && val instanceof Double) {
                    double pct = (Double) val;
                    setForeground(pct < threshold ? UIUtil.DANGER : UIUtil.SUCCESS);
                    setFont(UIUtil.FONT_BOLD);
                }
                return this;
            }
        });

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    if ("⚠ Low".equals(val)) { setForeground(UIUtil.DANGER); setFont(UIUtil.FONT_BOLD); }
                    else { setForeground(UIUtil.SUCCESS); setFont(UIUtil.FONT_BODY); }
                }
                return this;
            }
        });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setOpaque(false);
        JButton refreshBtn = UIUtil.primaryButton("Refresh");
        JButton exportBtn  = UIUtil.exportButton(this, table, "subject_attendance_" + student.getStudentCode());
        btnBar.add(refreshBtn);
        btnBar.add(exportBtn);

        JLabel summaryLabel = UIUtil.label(" ");
        summaryLabel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(btnBar, BorderLayout.NORTH);
        center.add(UIUtil.scrollPane(table), BorderLayout.CENTER);
        center.add(summaryLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        Runnable load = () -> {
            model.setRowCount(0);
            SwingWorker<List<Object[]>, Void> w = new SwingWorker<List<Object[]>, Void>() {
                @Override protected List<Object[]> doInBackground() throws Exception {
                    return attendanceDAO.getStudentSubjectBreakdown(student.getId());
                }
                @Override protected void done() {
                    try {
                        List<Object[]> rows = get();
                        int low = 0;
                        for (Object[] row : rows) {
                            double pct = (double) row[6];
                            String status = pct < threshold ? "⚠ Low" : "✓ OK";
                            if (pct < threshold) low++;
                            model.addRow(new Object[]{row[0], row[1], row[2], row[3], row[4], row[5], pct, status});
                        }
                        summaryLabel.setText(String.format("Subjects: %d | Below threshold: %d", rows.size(), low));
                    } catch (Exception ex) { UIUtil.showError(SubjectBreakdownPanel.this, ex.getMessage()); }
                }
            };
            w.execute();
        };

        refreshBtn.addActionListener(e -> load.run());
        // exportBtn popup is wired inside UIUtil.exportButton()

        load.run();
    }
}
