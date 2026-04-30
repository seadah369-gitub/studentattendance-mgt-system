package com.attendance.ui.student;

import com.attendance.dao.AttendanceDAO;
import com.attendance.model.Student;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

public class AttendanceCalendarPanel extends JPanel {

    private final Student student;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private JPanel calendarGrid;
    private JLabel monthLabel;
    private YearMonth currentMonth;
    private final Map<LocalDate, String> attendanceMap = new HashMap<>();

    public AttendanceCalendarPanel(Student student) {
        this.student = student;
        currentMonth = YearMonth.now();
        setLayout(new BorderLayout());
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = UIUtil.headerLabel("Monthly Attendance Calendar");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        navBar.setOpaque(false);
        JButton prevBtn = UIUtil.secondaryButton("◀ Prev");
        JButton nextBtn = UIUtil.secondaryButton("Next ▶");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        monthLabel.setPreferredSize(new Dimension(200, 30));
        navBar.add(prevBtn);
        navBar.add(monthLabel);
        navBar.add(nextBtn);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        legend.setOpaque(false);
        legend.add(legendItem("Present", UIUtil.SUCCESS));
        legend.add(legendItem("Absent",  UIUtil.DANGER));
        legend.add(legendItem("Late",    UIUtil.WARNING));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(navBar, BorderLayout.NORTH);
        topPanel.add(legend, BorderLayout.SOUTH);

        calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGrid.setOpaque(false);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(topPanel, BorderLayout.NORTH);
        center.add(calendarGrid, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        prevBtn.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); loadMonth(); });
        nextBtn.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); loadMonth(); });

        loadMonth();
    }

    private void loadMonth() {
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            + " " + currentMonth.getYear());

        SwingWorker<List<Object[]>, Void> w = new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                return attendanceDAO.getMonthlyAttendance(
                    student.getId(), currentMonth.getYear(), currentMonth.getMonthValue());
            }
            @Override protected void done() {
                try {
                    attendanceMap.clear();
                    for (Object[] row : get()) {
                        LocalDate date = (LocalDate) row[0];
                        String status  = (String) row[1];
                        attendanceMap.merge(date, status, (existing, newVal) -> {
                            if ("ABSENT".equals(existing) || "ABSENT".equals(newVal)) return "ABSENT";
                            if ("LATE".equals(existing)   || "LATE".equals(newVal))   return "LATE";
                            return "PRESENT";
                        });
                    }
                    buildCalendar();
                } catch (Exception ex) { UIUtil.showError(AttendanceCalendarPanel.this, ex.getMessage()); }
            }
        };
        w.execute();
    }

    private void buildCalendar() {
        calendarGrid.removeAll();
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(UIUtil.FONT_BOLD);
            lbl.setForeground(UIUtil.TEXT_SECONDARY);
            calendarGrid.add(lbl);
        }
        LocalDate first = currentMonth.atDay(1);
        int startDow = first.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < startDow; i++) calendarGrid.add(new JLabel());
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            calendarGrid.add(buildDayCell(day, attendanceMap.get(date), date.equals(LocalDate.now())));
        }
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JPanel buildDayCell(int day, String status, boolean isToday) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setPreferredSize(new Dimension(52, 52));
        cell.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 230)));
        Color bg = new Color(248, 248, 255);
        if ("PRESENT".equals(status))      bg = new Color(200, 240, 200);
        else if ("ABSENT".equals(status))  bg = new Color(255, 210, 210);
        else if ("LATE".equals(status))    bg = new Color(255, 235, 180);
        cell.setBackground(bg);
        if (isToday) cell.setBorder(BorderFactory.createLineBorder(UIUtil.PRIMARY, 2));
        JLabel dayLbl = new JLabel(String.valueOf(day), SwingConstants.CENTER);
        dayLbl.setFont(isToday ? UIUtil.FONT_BOLD : UIUtil.FONT_BODY);
        dayLbl.setForeground(isToday ? UIUtil.PRIMARY : UIUtil.TEXT_PRIMARY);
        cell.add(dayLbl, BorderLayout.CENTER);
        if (status != null) {
            JLabel s = new JLabel(status.substring(0, 1), SwingConstants.CENTER);
            s.setFont(UIUtil.FONT_SMALL);
            s.setForeground(UIUtil.TEXT_SECONDARY);
            cell.add(s, BorderLayout.SOUTH);
        }
        return cell;
    }

    private JPanel legendItem(String label, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setPreferredSize(new Dimension(14, 14));
        p.add(dot);
        p.add(UIUtil.label(label));
        return p;
    }
}
