package com.attendance.ui.teacher;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.ClassCourseDAO;
import com.attendance.model.ClassCourse;
import com.attendance.model.Teacher;
import com.attendance.service.SystemConfigService;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherReportPanel extends JPanel {

    private final Teacher teacher;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ClassCourseDAO csDAO        = new ClassCourseDAO();
    private final SystemConfigService configSvc = new SystemConfigService();

    private JComboBox<String> csCombo;
    private List<ClassCourse> csList = new ArrayList<>();

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel summaryLabel;
    private JToggleButton lowFilterBtn;   // sticky toggle for low-attendance filter
    private JLabel thresholdLabel;        // shows current threshold value

    // All loaded rows — kept so we can re-apply filter without re-querying DB
    private final List<Object[]> allRows = new ArrayList<>();

    public TeacherReportPanel(Teacher teacher) {
        this.teacher = teacher;
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = UIUtil.headerLabel("Class Attendance Report");
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        // ---- Table (must be created before exportBtn) ----
        String[] cols = {"Student Name", "Student Code", "Total", "Present", "Absent", "Late", "Percentage %"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        reportTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        reportTable.setRowSorter(sorter);
        UIUtil.styleTable(reportTable);
        reportTable.setRowHeight(32);

        // Color-code rows: red bg if below threshold, green % text if above
        reportTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!sel) {
                    int modelRow = t.convertRowIndexToModel(row);
                    Object pct = tableModel.getValueAt(modelRow, 6);
                    double threshold = configSvc.getAttendanceThreshold();
                    boolean low = pct instanceof Double && (Double) pct < threshold;
                    setBackground(low ? new Color(255, 235, 235)
                                      : row % 2 == 0 ? Color.WHITE : new Color(248, 248, 255));
                    // Bold red/green on the percentage column
                    if (col == 6 && pct instanceof Double) {
                        setFont(UIUtil.FONT_BOLD);
                        setForeground(low ? UIUtil.DANGER : UIUtil.SUCCESS);
                    } else {
                        setFont(UIUtil.FONT_BODY);
                        setForeground(UIUtil.TEXT_PRIMARY);
                    }
                }
                return this;
            }
        });

        // ---- Filter bar ----
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(0, 0, 6, 0));

        csCombo = new JComboBox<>();
        csCombo.setFont(UIUtil.FONT_BODY);
        csCombo.setPreferredSize(new Dimension(300, 34));

        JButton generateBtn = UIUtil.primaryButton("Generate");
        JButton refreshBtn  = UIUtil.secondaryButton("↺ Refresh");

        // Low-attendance toggle button
        lowFilterBtn = new JToggleButton("⚠ Low Attendance Only");
        lowFilterBtn.setFont(UIUtil.FONT_BOLD);
        lowFilterBtn.setForeground(Color.WHITE);
        lowFilterBtn.setBackground(UIUtil.DANGER);
        lowFilterBtn.setFocusPainted(false);
        lowFilterBtn.setBorderPainted(false);
        lowFilterBtn.setOpaque(true);
        lowFilterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lowFilterBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        lowFilterBtn.setEnabled(false); // enabled after report is generated

        // Threshold indicator
        double threshold = configSvc.getAttendanceThreshold();
        thresholdLabel = new JLabel("Threshold: " + (int) threshold + "%");
        thresholdLabel.setFont(UIUtil.FONT_SMALL);
        thresholdLabel.setForeground(UIUtil.TEXT_SECONDARY);
        thresholdLabel.setBorder(new EmptyBorder(0, 4, 0, 0));

        JButton exportBtn = UIUtil.exportButton(this, reportTable, "class_report");

        filterBar.add(UIUtil.boldLabel("Class:"));
        filterBar.add(csCombo);
        filterBar.add(generateBtn);
        filterBar.add(refreshBtn);
        filterBar.add(Box.createHorizontalStrut(4));
        filterBar.add(lowFilterBtn);
        filterBar.add(thresholdLabel);
        filterBar.add(Box.createHorizontalStrut(4));
        filterBar.add(exportBtn);

        // ---- Summary label ----
        summaryLabel = UIUtil.label(" ");
        summaryLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        // ---- Layout ----
        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        center.add(filterBar, BorderLayout.NORTH);
        center.add(UIUtil.scrollPane(reportTable), BorderLayout.CENTER);
        center.add(summaryLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // ---- Wire ----
        loadCombo();

        generateBtn.addActionListener(e -> generateReport());
        refreshBtn.addActionListener(e -> { loadCombo(); });

        lowFilterBtn.addActionListener(e -> applyFilter());
    }

    // ---- Combo ----

    private void loadCombo() {
        csCombo.removeAllItems();
        csList.clear();
        try {
            List<ClassCourse> classes = csDAO.findByTeacher(teacher.getId());
            csCombo.addItem("— ALL My Classes —");
            csList.add(null);
            for (ClassCourse cs : classes) {
                csCombo.addItem(cs.getClassName() + "  /  " + cs.getCourseName()
                    + " (" + cs.getCourseCode() + ")");
                csList.add(cs);
            }
            if (classes.isEmpty()) {
                summaryLabel.setText("⚠  No classes assigned. Ask admin to assign you to a class.");
                summaryLabel.setForeground(UIUtil.WARNING);
            }
        } catch (Exception ignored) {}
        // Refresh threshold label in case admin changed it
        double t = configSvc.getAttendanceThreshold();
        thresholdLabel.setText("Threshold: " + (int) t + "%");
    }

    // ---- Generate ----

    private void generateReport() {
        int idx = csCombo.getSelectedIndex();
        if (idx < 0) { UIUtil.showError(this, "Select a class first."); return; }

        tableModel.setRowCount(0);
        allRows.clear();
        lowFilterBtn.setSelected(false);
        lowFilterBtn.setEnabled(false);
        summaryLabel.setText("Loading...");
        summaryLabel.setForeground(UIUtil.TEXT_SECONDARY);

        ClassCourse selected = csList.get(idx); // null = ALL

        SwingWorker<List<Object[]>, Void> w = new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                if (selected == null) {
                    List<Object[]> combined = new ArrayList<>();
                    for (ClassCourse cs : csDAO.findByTeacher(teacher.getId()))
                        combined.addAll(attendanceDAO.getClassAttendanceSummary(cs.getId()));
                    return combined;
                }
                return attendanceDAO.getClassAttendanceSummary(selected.getId());
            }
            @Override protected void done() {
                try {
                    List<Object[]> rows = get();
                    allRows.addAll(rows);

                    double threshold = configSvc.getAttendanceThreshold();
                    int low = 0;
                    for (Object[] row : rows) {
                        tableModel.addRow(row);
                        if (row[6] instanceof Double && (Double) row[6] < threshold) low++;
                    }

                    String scope = selected == null ? "All My Classes"
                        : selected.getClassName() + " / " + selected.getCourseName();

                    updateSummary(scope, rows.size(), low, threshold, false);
                    lowFilterBtn.setEnabled(true);

                } catch (Exception ex) {
                    summaryLabel.setText("Error: " + ex.getMessage());
                    summaryLabel.setForeground(UIUtil.DANGER);
                }
            }
        };
        w.execute();
    }

    // ---- Low-attendance filter ----

    private void applyFilter() {
        if (allRows.isEmpty()) return;

        double threshold = configSvc.getAttendanceThreshold();
        boolean showLowOnly = lowFilterBtn.isSelected();

        tableModel.setRowCount(0);
        int shown = 0, low = 0;

        for (Object[] row : allRows) {
            boolean isLow = row[6] instanceof Double && (Double) row[6] < threshold;
            if (isLow) low++;
            if (!showLowOnly || isLow) {
                tableModel.addRow(row);
                shown++;
            }
        }

        // Update toggle button appearance
        if (showLowOnly) {
            lowFilterBtn.setText("✓ Low Attendance Only");
            lowFilterBtn.setBackground(new Color(180, 30, 30));
        } else {
            lowFilterBtn.setText("⚠ Low Attendance Only");
            lowFilterBtn.setBackground(UIUtil.DANGER);
        }

        int idx = csCombo.getSelectedIndex();
        ClassCourse selected = (idx >= 0) ? csList.get(idx) : null;
        String scope = selected == null ? "All My Classes"
            : selected.getClassName() + " / " + selected.getCourseName();

        updateSummary(scope, shown, low, threshold, showLowOnly);
    }

    private void updateSummary(String scope, int total, int low, double threshold, boolean filtered) {
        String filterNote = filtered ? "  (showing low only)" : "";
        summaryLabel.setText(scope + "  |  " + total + " students"
            + "  |  Below " + (int) threshold + "%: " + low + filterNote);
        summaryLabel.setForeground(low > 0 ? UIUtil.DANGER : UIUtil.SUCCESS);
    }
}
