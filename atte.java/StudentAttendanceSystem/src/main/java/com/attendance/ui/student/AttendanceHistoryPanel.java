package com.attendance.ui.student;

import com.attendance.dao.AttendanceDAO;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.Student;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AttendanceHistoryPanel extends JPanel {

    private final Student student;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    public AttendanceHistoryPanel(Student student) {
        this.student = student;
        setLayout(new BorderLayout());
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = UIUtil.headerLabel("Full Attendance History");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Date", "Subject", "Class", "Status", "Remark"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtil.styleTable(table);
        table.setRowHeight(30);

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    String s = String.valueOf(val);
                    if ("PRESENT".equals(s)) { setForeground(UIUtil.SUCCESS); setFont(UIUtil.FONT_BOLD); }
                    else if ("ABSENT".equals(s)) { setForeground(UIUtil.DANGER); setFont(UIUtil.FONT_BOLD); }
                    else if ("LATE".equals(s)) { setForeground(UIUtil.WARNING); setFont(UIUtil.FONT_BOLD); }
                    else { setForeground(UIUtil.TEXT_PRIMARY); }
                }
                return this;
            }
        });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setOpaque(false);
        JButton refreshBtn = UIUtil.primaryButton("Refresh");
        JButton exportBtn  = UIUtil.exportButton(this, table, "attendance_history_" + student.getStudentCode());
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
            tableModel.setRowCount(0);
            SwingWorker<List<AttendanceRecord>, Void> w = new SwingWorker<List<AttendanceRecord>, Void>() {
                @Override protected List<AttendanceRecord> doInBackground() throws Exception {
                    return attendanceDAO.findRecordsByStudent(student.getId());
                }
                @Override protected void done() {
                    try {
                        List<AttendanceRecord> records = get();
                        int present = 0, absent = 0, late = 0;
                        for (AttendanceRecord r : records) {
                            tableModel.addRow(new Object[]{
                                r.getSessionDate(), r.getSubjectName(), r.getClassName(),
                                r.getStatus(), r.getRemark()});
                            if ("PRESENT".equals(r.getStatus())) present++;
                            else if ("ABSENT".equals(r.getStatus())) absent++;
                            else if ("LATE".equals(r.getStatus())) late++;
                        }
                        summaryLabel.setText(String.format(
                            "Total: %d | Present: %d | Absent: %d | Late: %d",
                            records.size(), present, absent, late));
                    } catch (Exception ex) { UIUtil.showError(AttendanceHistoryPanel.this, ex.getMessage()); }
                }
            };
            w.execute();
        };

        refreshBtn.addActionListener(e -> load.run());
        // exportBtn popup is wired inside UIUtil.exportButton()

        load.run();
    }
}
