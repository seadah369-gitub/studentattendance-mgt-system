package com.attendance.ui.teacher;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.ClassCourseDAO;
import com.attendance.model.AttendanceSession;
import com.attendance.model.ClassCourse;
import com.attendance.model.Teacher;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TeacherDashboardPanel extends JPanel {

    private final Teacher teacher;
    private final AttendanceDAO  attendanceDAO = new AttendanceDAO();
    private final ClassCourseDAO ccDAO         = new ClassCourseDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Sessions table
    private DefaultTableModel sessionsModel;
    private JTable sessionsTable;
    private TableRowSorter<DefaultTableModel> sessionsSorter;
    private JLabel sessionsCountLabel;

    // Date range controls
    private JSpinner fromSpinner, toSpinner;
    private JLabel rangeLabel;

    // Stat labels
    private JLabel classCountLbl, sessionCountLbl;

    public TeacherDashboardPanel(Teacher teacher) {
        this.teacher = teacher;
        setLayout(new BorderLayout());
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // ---- Header ----
        JLabel title = UIUtil.headerLabel("Welcome, " + teacher.getFirstName());
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // ---- Stat cards ----
        classCountLbl   = new JLabel("...");
        sessionCountLbl = new JLabel("...");

        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        statsRow.setOpaque(false);
        statsRow.add(buildStatCard("My Classes",       classCountLbl,   new Color(63, 81, 181)));
        statsRow.add(buildStatCard("Sessions in Range", sessionCountLbl, new Color(0, 150, 136)));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(statsRow);
        center.add(Box.createVerticalStrut(20));

        // ---- Sessions card with date range filter ----
        JPanel sessionsCard = UIUtil.card(null);
        sessionsCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Card header row: title + date range controls
        JPanel cardHeader = new JPanel(new BorderLayout(12, 0));
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel sessionsTitle = new JLabel("Attendance Sessions");
        sessionsTitle.setFont(UIUtil.FONT_HEADER);
        sessionsTitle.setForeground(UIUtil.PRIMARY);

        JPanel filterRow = buildDateRangeBar();
        cardHeader.add(sessionsTitle, BorderLayout.WEST);
        cardHeader.add(filterRow,     BorderLayout.EAST);
        sessionsCard.add(cardHeader, BorderLayout.NORTH);

        // Sessions table
        String[] sCols = {"ID", "Class", "Course", "Date", "Records"};
        sessionsModel = new DefaultTableModel(sCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sessionsTable = new JTable(sessionsModel);
        sessionsSorter = new TableRowSorter<>(sessionsModel);
        sessionsTable.setRowSorter(sessionsSorter);
        UIUtil.styleTable(sessionsTable);
        sessionsTable.setRowHeight(30);

        // Hide ID column
        sessionsTable.getColumnModel().getColumn(0).setMinWidth(0);
        sessionsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        sessionsTable.getColumnModel().getColumn(0).setWidth(0);
        // Records column — narrow
        sessionsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        sessionsTable.getColumnModel().getColumn(4).setMaxWidth(100);

        // Count label below table
        sessionsCountLabel = new JLabel("0 sessions");
        sessionsCountLabel.setFont(UIUtil.FONT_SMALL);
        sessionsCountLabel.setForeground(UIUtil.TEXT_SECONDARY);
        sessionsCountLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        sessionsCard.add(UIUtil.scrollPane(sessionsTable), BorderLayout.CENTER);
        sessionsCard.add(sessionsCountLabel, BorderLayout.SOUTH);
        center.add(sessionsCard);
        center.add(Box.createVerticalStrut(16));

        // ---- My Assigned Classes card ----
        JPanel classCard = UIUtil.card("My Assigned Classes");
        classCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] cCols = {"Class", "Course", "Code"};
        DefaultTableModel classModel = new DefaultTableModel(cCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable classTable = new JTable(classModel);
        UIUtil.styleTable(classTable);
        classCard.add(UIUtil.scrollPane(classTable), BorderLayout.CENTER);
        center.add(classCard);

        add(center, BorderLayout.CENTER);

        // ---- Load assigned classes (once) ----
        SwingWorker<List<ClassCourse>, Void> classWorker = new SwingWorker<>() {
            @Override protected List<ClassCourse> doInBackground() throws Exception {
                return ccDAO.findByTeacher(teacher.getId());
            }
            @Override protected void done() {
                try {
                    List<ClassCourse> classes = get();
                    classCountLbl.setText(String.valueOf(classes.size()));
                    for (ClassCourse cc : classes)
                        classModel.addRow(new Object[]{cc.getClassName(), cc.getCourseName(), cc.getCourseCode()});
                } catch (Exception ignored) {}
            }
        };
        classWorker.execute();

        // ---- Load sessions for default range (today) ----
        loadSessions();
    }

    // ---- Date range bar ----

    private JPanel buildDateRangeBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bar.setOpaque(false);

        // Default: today
        LocalDate today = LocalDate.now();
        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel   = new SpinnerDateModel();

        fromSpinner = new JSpinner(fromModel);
        toSpinner   = new JSpinner(toModel);

        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd"));
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner,     "yyyy-MM-dd"));

        // Set default values
        java.util.Date todayDate = java.util.Date.from(
            today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        fromSpinner.setValue(todayDate);
        toSpinner.setValue(todayDate);

        fromSpinner.setPreferredSize(new Dimension(120, 30));
        toSpinner.setPreferredSize(new Dimension(120, 30));

        JButton todayBtn   = UIUtil.secondaryButton("Today");
        JButton weekBtn    = UIUtil.secondaryButton("This Week");
        JButton monthBtn   = UIUtil.secondaryButton("This Month");
        JButton allBtn     = UIUtil.secondaryButton("All Time");
        JButton applyBtn   = UIUtil.primaryButton("Apply");

        // Make quick-range buttons compact
        for (JButton b : new JButton[]{todayBtn, weekBtn, monthBtn, allBtn}) {
            b.setFont(UIUtil.FONT_SMALL);
            b.setBorder(new EmptyBorder(4, 8, 4, 8));
        }

        rangeLabel = new JLabel("Today");
        rangeLabel.setFont(UIUtil.FONT_SMALL);
        rangeLabel.setForeground(UIUtil.TEXT_SECONDARY);

        bar.add(UIUtil.boldLabel("From:"));
        bar.add(fromSpinner);
        bar.add(UIUtil.boldLabel("To:"));
        bar.add(toSpinner);
        bar.add(applyBtn);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(todayBtn);
        bar.add(weekBtn);
        bar.add(monthBtn);
        bar.add(allBtn);
        bar.add(rangeLabel);

        // Quick range buttons
        todayBtn.addActionListener(e -> {
            setRange(today, today);
            rangeLabel.setText("Today");
            loadSessions();
        });
        weekBtn.addActionListener(e -> {
            LocalDate mon = today.with(java.time.DayOfWeek.MONDAY);
            LocalDate sun = today.with(java.time.DayOfWeek.SUNDAY);
            setRange(mon, sun);
            rangeLabel.setText("This Week");
            loadSessions();
        });
        monthBtn.addActionListener(e -> {
            LocalDate first = today.withDayOfMonth(1);
            LocalDate last  = today.withDayOfMonth(today.lengthOfMonth());
            setRange(first, last);
            rangeLabel.setText("This Month");
            loadSessions();
        });
        allBtn.addActionListener(e -> {
            // Use a wide range — 10 years back to today
            setRange(today.minusYears(10), today);
            rangeLabel.setText("All Time");
            loadSessions();
        });
        applyBtn.addActionListener(e -> {
            rangeLabel.setText("Custom");
            loadSessions();
        });

        return bar;
    }

    /** Sets both spinners to the given dates. */
    private void setRange(LocalDate from, LocalDate to) {
        java.util.Date f = java.util.Date.from(from.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Date t = java.util.Date.from(to.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        fromSpinner.setValue(f);
        toSpinner.setValue(t);
    }

    /** Reads the spinner values and returns a LocalDate. */
    private LocalDate spinnerDate(JSpinner spinner) {
        java.util.Date d = (java.util.Date) spinner.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    // ---- Load sessions ----

    private void loadSessions() {
        sessionsModel.setRowCount(0);
        sessionCountLbl.setText("...");
        sessionsCountLabel.setText("Loading...");
        sessionsCountLabel.setForeground(UIUtil.TEXT_SECONDARY);

        LocalDate from = spinnerDate(fromSpinner);
        LocalDate to   = spinnerDate(toSpinner);

        // Validate range
        if (from.isAfter(to)) {
            UIUtil.showError(this, "\"From\" date cannot be after \"To\" date.");
            return;
        }

        SwingWorker<List<AttendanceSession>, Void> w = new SwingWorker<>() {
            @Override protected List<AttendanceSession> doInBackground() throws Exception {
                return attendanceDAO.findSessionsByTeacherAndRange(teacher.getId(), from, to);
            }
            @Override protected void done() {
                try {
                    List<AttendanceSession> sessions = get();
                    for (AttendanceSession s : sessions) {
                        int count = 0;
                        try { count = attendanceDAO.findRecordsBySession(s.getId()).size(); }
                        catch (Exception ignored) {}
                        sessionsModel.addRow(new Object[]{
                            s.getId(), s.getClassName(), s.getSubjectName(),
                            s.getDate().format(FMT), count + " students"
                        });
                    }
                    sessionCountLbl.setText(String.valueOf(sessions.size()));

                    String rangeStr = from.equals(to)
                        ? from.format(FMT)
                        : from.format(FMT) + " → " + to.format(FMT);

                    sessionsCountLabel.setText(sessions.size() + " session(s)  |  " + rangeStr);
                    sessionsCountLabel.setForeground(
                        sessions.isEmpty() ? UIUtil.TEXT_SECONDARY : UIUtil.SUCCESS);

                } catch (Exception ex) {
                    sessionsCountLabel.setText("Error: " + ex.getMessage());
                    sessionsCountLabel.setForeground(UIUtil.DANGER);
                }
            }
        };
        w.execute();
    }

    // ---- Stat card builder ----

    private JPanel buildStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setPreferredSize(new Dimension(200, 100));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(Color.WHITE);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(255, 255, 255, 200));
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        return card;
    }
}
