package com.attendance.ui.teacher;

import com.attendance.dao.AttendanceDAO;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.AttendanceSession;
import com.attendance.model.Teacher;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EditAttendancePanel extends JPanel {

    private final Teacher teacher;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private JTable sessionsTable, recordsTable;
    private DefaultTableModel sessionsModel, recordsModel;
    private JButton deleteSessionBtn, saveBtn;
    private JLabel infoLabel;

    public EditAttendancePanel(Teacher teacher) {
        this.teacher = teacher;
        setLayout(new BorderLayout());
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = UIUtil.headerLabel("Edit Attendance Records");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        // ---- Sessions table ----
        String[] sCols = {"ID", "Class", "Course", "Date", "Records"};
        sessionsModel = new DefaultTableModel(sCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sessionsTable = new JTable(sessionsModel);
        UIUtil.styleTable(sessionsTable);
        sessionsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide ID column
        sessionsTable.getColumnModel().getColumn(0).setMinWidth(0);
        sessionsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        sessionsTable.getColumnModel().getColumn(0).setWidth(0);
        // Records count column — narrow
        sessionsTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        sessionsTable.getColumnModel().getColumn(4).setMaxWidth(90);

        // Sessions toolbar
        JPanel sessionsToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        sessionsToolbar.setOpaque(false);
        JButton refreshBtn    = UIUtil.secondaryButton("↺ Refresh");
        deleteSessionBtn      = UIUtil.dangerButton("🗑 Delete Session");
        deleteSessionBtn.setEnabled(false);
        sessionsToolbar.add(refreshBtn);
        sessionsToolbar.add(deleteSessionBtn);

        JPanel sessionsCard = UIUtil.card("My Sessions  —  select one to view / edit records");
        sessionsCard.add(sessionsToolbar, BorderLayout.NORTH);
        sessionsCard.add(UIUtil.scrollPane(sessionsTable), BorderLayout.CENTER);

        // ---- Records table ----
        String[] rCols = {"ID", "Student Code", "Student Name", "Status", "Remark"};
        recordsModel = new DefaultTableModel(rCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3 || c == 4; }
        };
        recordsTable = new JTable(recordsModel);
        UIUtil.styleTable(recordsTable);
        recordsTable.setRowHeight(32);

        // Hide record ID column
        recordsTable.getColumnModel().getColumn(0).setMinWidth(0);
        recordsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        recordsTable.getColumnModel().getColumn(0).setWidth(0);

        // Status dropdown editor
        JComboBox<String> statusEditor = new JComboBox<>(new String[]{"PRESENT", "ABSENT", "LATE"});
        recordsTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusEditor));
        recordsTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        recordsTable.getColumnModel().getColumn(3).setMaxWidth(110);

        // Color-code status column
        recordsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
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

        // Records toolbar
        infoLabel = new JLabel("Select a session above to load its records.");
        infoLabel.setFont(UIUtil.FONT_SMALL);
        infoLabel.setForeground(UIUtil.TEXT_SECONDARY);

        JPanel recordsToolbar = new JPanel(new BorderLayout(8, 0));
        recordsToolbar.setOpaque(false);
        recordsToolbar.setBorder(new EmptyBorder(0, 0, 4, 0));
        saveBtn = UIUtil.primaryButton("💾 Save Changes");
        saveBtn.setEnabled(false);
        JPanel saveBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        saveBtnPanel.setOpaque(false);
        saveBtnPanel.add(saveBtn);
        recordsToolbar.add(infoLabel, BorderLayout.CENTER);
        recordsToolbar.add(saveBtnPanel, BorderLayout.EAST);

        JPanel recordsCard = UIUtil.card("Attendance Records");
        recordsCard.add(recordsToolbar, BorderLayout.NORTH);
        recordsCard.add(UIUtil.scrollPane(recordsTable), BorderLayout.CENTER);

        // ---- Split ----
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sessionsCard, recordsCard);
        split.setResizeWeight(0.38);
        split.setDividerSize(8);
        add(split, BorderLayout.CENTER);

        // ---- Load ----
        loadSessions();

        // ---- Wire ----
        sessionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = sessionsTable.getSelectedRow();
            if (viewRow < 0) {
                recordsModel.setRowCount(0);
                deleteSessionBtn.setEnabled(false);
                saveBtn.setEnabled(false);
                infoLabel.setText("Select a session above to load its records.");
                infoLabel.setForeground(UIUtil.TEXT_SECONDARY);
                return;
            }
            int modelRow = sessionsTable.convertRowIndexToModel(viewRow);
            long sessionId = (long) sessionsModel.getValueAt(modelRow, 0);
            loadRecords(sessionId);
            deleteSessionBtn.setEnabled(true);
            saveBtn.setEnabled(true);
        });

        refreshBtn.addActionListener(e -> loadSessions());

        deleteSessionBtn.addActionListener(e -> deleteSelectedSession());

        saveBtn.addActionListener(e -> saveChanges());
    }

    // ---- Load sessions ----

    private void loadSessions() {
        sessionsModel.setRowCount(0);
        recordsModel.setRowCount(0);
        deleteSessionBtn.setEnabled(false);
        saveBtn.setEnabled(false);
        infoLabel.setText("Loading sessions...");
        infoLabel.setForeground(UIUtil.TEXT_SECONDARY);

        SwingWorker<List<AttendanceSession>, Void> w = new SwingWorker<>() {
            @Override protected List<AttendanceSession> doInBackground() throws Exception {
                return attendanceDAO.findSessionsByTeacher(teacher.getId());
            }
            @Override protected void done() {
                try {
                    List<AttendanceSession> sessions = get();
                    for (AttendanceSession s : sessions) {
                        // Count records for this session
                        int count = 0;
                        try { count = attendanceDAO.findRecordsBySession(s.getId()).size(); }
                        catch (Exception ignored) {}
                        sessionsModel.addRow(new Object[]{
                            s.getId(), s.getClassName(), s.getSubjectName(),
                            s.getDate(), count + " students"
                        });
                    }
                    infoLabel.setText(sessions.isEmpty()
                        ? "No sessions found. Mark attendance first."
                        : "Select a session above to load its records.");
                    infoLabel.setForeground(sessions.isEmpty() ? UIUtil.WARNING : UIUtil.TEXT_SECONDARY);
                } catch (Exception ex) {
                    UIUtil.showError(EditAttendancePanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }

    // ---- Load records ----

    private void loadRecords(long sessionId) {
        recordsModel.setRowCount(0);
        infoLabel.setText("Loading records...");
        infoLabel.setForeground(UIUtil.TEXT_SECONDARY);

        SwingWorker<List<AttendanceRecord>, Void> w = new SwingWorker<>() {
            @Override protected List<AttendanceRecord> doInBackground() throws Exception {
                return attendanceDAO.findRecordsBySession(sessionId);
            }
            @Override protected void done() {
                try {
                    List<AttendanceRecord> records = get();
                    for (AttendanceRecord r : records)
                        recordsModel.addRow(new Object[]{
                            r.getId(), r.getStudentCode(), r.getStudentName(),
                            r.getStatus(), r.getRemark() != null ? r.getRemark() : ""
                        });
                    infoLabel.setText(records.size() + " record(s) — edit Status or Remark, then click Save.");
                    infoLabel.setForeground(UIUtil.TEXT_SECONDARY);
                } catch (Exception ex) {
                    UIUtil.showError(EditAttendancePanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }

    // ---- Save changes ----

    private void saveChanges() {
        if (recordsModel.getRowCount() == 0) { UIUtil.showError(this, "No records to save."); return; }
        if (recordsTable.isEditing()) recordsTable.getCellEditor().stopCellEditing();

        // Snapshot on EDT before handing to background thread
        final int rowCount = recordsModel.getRowCount();
        final long[]   ids      = new long[rowCount];
        final String[] statuses = new String[rowCount];
        final String[] remarks  = new String[rowCount];
        for (int r = 0; r < rowCount; r++) {
            ids[r]      = (long)   recordsModel.getValueAt(r, 0);
            statuses[r] = (String) recordsModel.getValueAt(r, 3);
            Object rem  =          recordsModel.getValueAt(r, 4);
            remarks[r]  = rem != null ? rem.toString() : "";
        }

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                for (int r = 0; r < rowCount; r++)
                    attendanceDAO.updateRecord(ids[r], statuses[r], remarks[r]);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    infoLabel.setText("✓ Changes saved successfully.");
                    infoLabel.setForeground(UIUtil.SUCCESS);
                    UIUtil.showSuccess(EditAttendancePanel.this, "Attendance records saved.");
                } catch (Exception ex) {
                    UIUtil.showError(EditAttendancePanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }

    // ---- Delete session ----

    private void deleteSelectedSession() {
        int viewRow = sessionsTable.getSelectedRow();
        if (viewRow < 0) { UIUtil.showError(this, "Select a session first."); return; }

        int modelRow  = sessionsTable.convertRowIndexToModel(viewRow);
        long sessionId = (long) sessionsModel.getValueAt(modelRow, 0);
        String cls    = (String) sessionsModel.getValueAt(modelRow, 1);
        String course = (String) sessionsModel.getValueAt(modelRow, 2);
        Object date   = sessionsModel.getValueAt(modelRow, 3);
        String count  = (String) sessionsModel.getValueAt(modelRow, 4);

        // Confirmation dialog with session details
        String msg = "<html><b>Delete this attendance session?</b><br><br>"
            + "Class: <b>" + cls + "</b><br>"
            + "Course: <b>" + course + "</b><br>"
            + "Date: <b>" + date + "</b><br>"
            + "Records: <b>" + count + "</b><br><br>"
            + "<font color='red'>This will permanently delete the session and all "
            + count + " attendance records. This cannot be undone.</font></html>";

        int choice = JOptionPane.showConfirmDialog(
            this, msg, "Confirm Delete Session",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return attendanceDAO.deleteSession(sessionId, teacher.getId());
            }
            @Override protected void done() {
                try {
                    boolean deleted = get();
                    if (deleted) {
                        loadSessions(); // refresh the sessions list
                        UIUtil.showSuccess(EditAttendancePanel.this,
                            "Session deleted successfully.");
                    } else {
                        UIUtil.showError(EditAttendancePanel.this,
                            "Could not delete session. You can only delete sessions you created.");
                    }
                } catch (Exception ex) {
                    UIUtil.showError(EditAttendancePanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }
}
