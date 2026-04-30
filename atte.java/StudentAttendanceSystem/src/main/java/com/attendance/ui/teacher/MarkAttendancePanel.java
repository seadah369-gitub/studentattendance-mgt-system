package com.attendance.ui.teacher;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.ClassCourseDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.ClassCourse;
import com.attendance.model.Student;
import com.attendance.model.Teacher;
import com.attendance.service.NotificationService;
import com.attendance.service.SystemConfigService;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkAttendancePanel extends JPanel {

    private final Teacher teacher;
    private final AttendanceDAO     attendanceDAO = new AttendanceDAO();
    private final ClassCourseDAO    ccDAO         = new ClassCourseDAO();
    private final StudentDAO        studentDAO    = new StudentDAO();
    private final NotificationService notifSvc    = new NotificationService();
    private final SystemConfigService configSvc   = new SystemConfigService();

    private JComboBox<ClassCourse> ccCombo;
    private JSpinner dateSpinner;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JLabel infoLabel;
    private long currentSessionId = -1;

    // Column indices
    private static final int COL_ID      = 0;
    private static final int COL_CODE    = 1;
    private static final int COL_NAME    = 2;
    private static final int COL_PCT     = 3;   // NEW: current attendance %
    private static final int COL_STATUS  = 4;
    private static final int COL_REMARK  = 5;

    public MarkAttendancePanel(Teacher teacher) {
        this.teacher = teacher;
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = UIUtil.headerLabel("Mark Attendance");
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        // ---- Controls bar ----
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(0, 0, 10, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        ccCombo = new JComboBox<>();
        ccCombo.setFont(UIUtil.FONT_BODY);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(java.util.Date.from(
            LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));

        JButton loadBtn    = UIUtil.primaryButton("Load Students");
        JButton markAllBtn = UIUtil.successButton("Mark All Present");
        JButton saveBtn    = UIUtil.saveButton("Save Attendance");

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        controls.add(UIUtil.boldLabel("Class-Course:"), g);
        g.gridx = 1; g.weightx = 1.0;
        controls.add(ccCombo, g);
        g.gridx = 2; g.weightx = 0;
        controls.add(UIUtil.boldLabel("Date:"), g);
        g.gridx = 3;
        controls.add(dateSpinner, g);
        g.gridx = 4; controls.add(loadBtn, g);
        g.gridx = 5; controls.add(markAllBtn, g);
        g.gridx = 6; controls.add(saveBtn, g);

        infoLabel = new JLabel(" ");
        infoLabel.setFont(UIUtil.FONT_BODY);
        infoLabel.setForeground(UIUtil.TEXT_SECONDARY);
        infoLabel.setBorder(new EmptyBorder(0, 0, 6, 0));

        // ---- Table ----
        // Columns: ID (hidden) | Code | Name | Current % | Status | Remark
        String[] cols = {"ID", "Student Code", "Name", "Current %", "Status", "Remark"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == COL_STATUS || c == COL_REMARK;
            }
        };
        attendanceTable = new JTable(tableModel);
        UIUtil.styleTable(attendanceTable);
        attendanceTable.setRowHeight(34);
        attendanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Hide ID column
        attendanceTable.getColumnModel().getColumn(COL_ID).setMinWidth(0);
        attendanceTable.getColumnModel().getColumn(COL_ID).setMaxWidth(0);
        attendanceTable.getColumnModel().getColumn(COL_ID).setWidth(0);

        // Current % column — narrow, right-aligned, color-coded
        attendanceTable.getColumnModel().getColumn(COL_PCT).setPreferredWidth(80);
        attendanceTable.getColumnModel().getColumn(COL_PCT).setMaxWidth(100);
        attendanceTable.getColumnModel().getColumn(COL_PCT).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(UIUtil.FONT_BOLD);
                if (!sel && val instanceof Double pct) {
                    double threshold = configSvc.getAttendanceThreshold();
                    if (pct == 0.0) {
                        setText("—");
                        setForeground(UIUtil.TEXT_SECONDARY);
                        setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 255));
                    } else if (pct < threshold) {
                        setText(String.format("%.0f%%", pct));
                        setForeground(UIUtil.DANGER);
                        setBackground(new Color(255, 235, 235));
                    } else {
                        setText(String.format("%.0f%%", pct));
                        setForeground(UIUtil.SUCCESS);
                        setBackground(new Color(235, 255, 235));
                    }
                } else if (val instanceof Double) {
                    setText(String.format("%.0f%%", (Double) val));
                }
                return this;
            }
        });

        // Status column — dropdown editor + color renderer
        attendanceTable.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(100);
        attendanceTable.getColumnModel().getColumn(COL_STATUS).setMaxWidth(120);

        JComboBox<String> statusEditor = new JComboBox<>(new String[]{"PRESENT", "ABSENT", "LATE"});
        attendanceTable.getColumnModel().getColumn(COL_STATUS).setCellEditor(new DefaultCellEditor(statusEditor));
        attendanceTable.getColumnModel().getColumn(COL_STATUS).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(UIUtil.FONT_BOLD);
                if (!sel) {
                    String s = String.valueOf(val);
                    switch (s) {
                        case "PRESENT" -> { setForeground(UIUtil.SUCCESS); setBackground(new Color(232, 255, 232)); }
                        case "ABSENT"  -> { setForeground(UIUtil.DANGER);  setBackground(new Color(255, 232, 232)); }
                        case "LATE"    -> { setForeground(UIUtil.WARNING); setBackground(new Color(255, 248, 220)); }
                        default        -> { setForeground(UIUtil.TEXT_PRIMARY); setBackground(Color.WHITE); }
                    }
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 230)));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Legend for % column
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.setBorder(new EmptyBorder(4, 0, 0, 0));
        legend.add(legendDot(UIUtil.DANGER,  "Below threshold"));
        legend.add(legendDot(UIUtil.SUCCESS, "Above threshold"));
        legend.add(legendDot(UIUtil.TEXT_SECONDARY, "No data yet"));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(controls, BorderLayout.CENTER);
        topBar.add(infoLabel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        center.add(topBar,     BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(legend,     BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // ---- Load class combo ----
        try {
            List<ClassCourse> classes = ccDAO.findByTeacher(teacher.getId());
            for (ClassCourse cc : classes) ccCombo.addItem(cc);
            if (classes.isEmpty()) {
                infoLabel.setText("⚠ No classes assigned. Ask admin to assign you to a class.");
                infoLabel.setForeground(UIUtil.WARNING);
            }
        } catch (Exception ignored) {}

        // ---- Wire ----
        loadBtn.addActionListener(e -> loadStudents());
        markAllBtn.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) { UIUtil.showError(this, "Load students first."); return; }
            for (int r = 0; r < tableModel.getRowCount(); r++) tableModel.setValueAt("PRESENT", r, COL_STATUS);
        });
        saveBtn.addActionListener(e -> saveAttendance());
    }

    // ---- Load students ----

    private void loadStudents() {
        ClassCourse cc = (ClassCourse) ccCombo.getSelectedItem();
        if (cc == null) { UIUtil.showError(this, "Select a class-course first."); return; }
        java.util.Date d = (java.util.Date) dateSpinner.getValue();
        LocalDate date = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        tableModel.setRowCount(0);
        infoLabel.setText("Loading...");
        infoLabel.setForeground(UIUtil.TEXT_SECONDARY);

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                currentSessionId = attendanceDAO.createSession(cc.getId(), date, teacher.getId());
                List<Student> students = studentDAO.findByClass(cc.getClassId());

                // Existing records for this session
                Map<Long, String[]> existMap = new HashMap<>();
                for (var rec : attendanceDAO.findRecordsBySession(currentSessionId))
                    existMap.put(rec.getStudentId(),
                        new String[]{rec.getStatus(), rec.getRemark() != null ? rec.getRemark() : ""});

                // Current attendance % per student (overall, not just this class)
                Map<Long, Double> pctMap = new HashMap<>();
                for (Student s : students) {
                    try { pctMap.put(s.getId(), attendanceDAO.getAttendancePercentage(s.getId())); }
                    catch (Exception ignored) { pctMap.put(s.getId(), 0.0); }
                }

                SwingUtilities.invokeLater(() -> {
                    if (students.isEmpty()) {
                        infoLabel.setText("⚠ No students enrolled. Go to Admin → Enrollments.");
                        infoLabel.setForeground(UIUtil.DANGER);
                    } else {
                        double threshold = configSvc.getAttendanceThreshold();
                        int atRisk = 0;
                        for (Student s : students) {
                            String[] ex  = existMap.getOrDefault(s.getId(), new String[]{"ABSENT", ""});
                            double   pct = pctMap.getOrDefault(s.getId(), 0.0);
                            if (pct > 0 && pct < threshold) atRisk++;
                            tableModel.addRow(new Object[]{
                                s.getId(), s.getStudentCode(), s.getFullName(),
                                pct,       // Double — rendered by custom renderer
                                ex[0], ex[1]
                            });
                        }
                        String atRiskNote = atRisk > 0
                            ? "  ⚠ " + atRisk + " student(s) below threshold"
                            : "";
                        infoLabel.setText("✓ " + students.size() + " students — "
                            + cc.getClassName() + " / " + cc.getCourseName()
                            + " — " + date + atRiskNote);
                        infoLabel.setForeground(atRisk > 0 ? UIUtil.WARNING : UIUtil.SUCCESS);
                    }
                });
                return null;
            }
            @Override protected void done() {
                try { get(); } catch (Exception ex) {
                    infoLabel.setText("Error: " + ex.getMessage());
                    infoLabel.setForeground(UIUtil.DANGER);
                }
            }
        };
        w.execute();
    }

    // ---- Save attendance ----

    private void saveAttendance() {
        if (currentSessionId < 0) { UIUtil.showError(this, "Load students first."); return; }
        if (tableModel.getRowCount() == 0) { UIUtil.showError(this, "No students to save."); return; }
        if (attendanceTable.isEditing()) attendanceTable.getCellEditor().stopCellEditing();

        // Snapshot on EDT
        final long   sessionSnap = currentSessionId;
        final int    rowCount    = tableModel.getRowCount();
        final long[] ids         = new long[rowCount];
        final String[] statuses  = new String[rowCount];
        final String[] remarks   = new String[rowCount];
        for (int r = 0; r < rowCount; r++) {
            ids[r]      = (long)   tableModel.getValueAt(r, COL_ID);
            statuses[r] = (String) tableModel.getValueAt(r, COL_STATUS);
            Object rem  =          tableModel.getValueAt(r, COL_REMARK);
            remarks[r]  = rem != null ? rem.toString() : "";
        }

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                for (int r = 0; r < rowCount; r++)
                    attendanceDAO.saveRecord(sessionSnap, ids[r], statuses[r], remarks[r]);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    // Auto-send low attendance notifications
                    double threshold = configSvc.getAttendanceThreshold();
                    for (int r = 0; r < rowCount; r++) {
                        double pct = attendanceDAO.getAttendancePercentage(ids[r]);
                        if (pct < threshold && pct > 0) {
                            Student s = studentDAO.findById(ids[r]);
                            if (s != null)
                                notifSvc.send(s.getUserId(),
                                    String.format("⚠ Your attendance is %.1f%% — below the %.0f%% threshold.", pct, threshold),
                                    "LOW_ATTENDANCE");
                        }
                    }
                    infoLabel.setText("✓ Attendance saved successfully!");
                    infoLabel.setForeground(UIUtil.SUCCESS);
                    UIUtil.showSuccess(MarkAttendancePanel.this, "Attendance saved.");
                } catch (Exception ex) { UIUtil.showError(MarkAttendancePanel.this, ex.getMessage()); }
            }
        };
        w.execute();
    }

    // ---- Legend helper ----

    private JPanel legendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setBorder(BorderFactory.createLineBorder(color.darker(), 1));
        p.add(dot);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtil.FONT_SMALL);
        lbl.setForeground(UIUtil.TEXT_SECONDARY);
        p.add(lbl);
        return p;
    }
}
