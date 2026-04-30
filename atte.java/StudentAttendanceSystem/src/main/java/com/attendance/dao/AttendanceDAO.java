package com.attendance.dao;

import com.attendance.config.DatabaseConfig;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.AttendanceSession;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    // ---- Sessions ----

    public long createSession(long classCourseId, LocalDate date, long teacherId) throws SQLException {
        String sql = "INSERT INTO attendance_sessions (class_course_id, date, created_by) VALUES (?,?,?) " +
                     "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, classCourseId);
            ps.setDate(2, Date.valueOf(date));
            ps.setLong(3, teacherId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return -1;
    }

    public AttendanceSession findSessionById(long id) throws SQLException {
        String sql = "SELECT ats.*, cl.name AS class_name, c.name AS subject_name, " +
                     "CONCAT(u.first_name,' ',u.last_name) AS teacher_name " +
                     "FROM attendance_sessions ats " +
                     "JOIN class_courses cc ON ats.class_course_id=cc.id " +
                     "JOIN classes cl ON cc.class_id=cl.id " +
                     "JOIN courses c ON cc.course_id=c.id " +
                     "LEFT JOIN teachers t ON ats.created_by=t.id " +
                     "LEFT JOIN users u ON t.user_id=u.id " +
                     "WHERE ats.id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSession(rs);
            }
        }
        return null;
    }

    public List<AttendanceSession> findSessionsByTeacher(long teacherId) throws SQLException {
        List<AttendanceSession> list = new ArrayList<>();
        String sql = "SELECT ats.*, cl.name AS class_name, c.name AS subject_name, " +
                     "CONCAT(u.first_name,' ',u.last_name) AS teacher_name " +
                     "FROM attendance_sessions ats " +
                     "JOIN class_courses cc ON ats.class_course_id=cc.id " +
                     "JOIN classes cl ON cc.class_id=cl.id " +
                     "JOIN courses c ON cc.course_id=c.id " +
                     "LEFT JOIN teachers t ON ats.created_by=t.id " +
                     "LEFT JOIN users u ON t.user_id=u.id " +
                     "WHERE cc.teacher_id=? ORDER BY ats.date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSession(rs));
            }
        }
        return list;
    }

    public List<AttendanceSession> findAllSessions(LocalDate from, LocalDate to) throws SQLException {
        List<AttendanceSession> list = new ArrayList<>();
        String sql = "SELECT ats.*, cl.name AS class_name, c.name AS subject_name, " +
                     "CONCAT(u.first_name,' ',u.last_name) AS teacher_name " +
                     "FROM attendance_sessions ats " +
                     "JOIN class_courses cc ON ats.class_course_id=cc.id " +
                     "JOIN classes cl ON cc.class_id=cl.id " +
                     "JOIN courses c ON cc.course_id=c.id " +
                     "LEFT JOIN teachers t ON ats.created_by=t.id " +
                     "LEFT JOIN users u ON t.user_id=u.id " +
                     "WHERE ats.date BETWEEN ? AND ? ORDER BY ats.date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSession(rs));
            }
        }
        return list;
    }

    /**
     * Returns sessions for a teacher filtered by date range (inclusive).
     * Pass null for either bound to make it open-ended.
     */
    public List<AttendanceSession> findSessionsByTeacherAndRange(
            long teacherId, LocalDate from, LocalDate to) throws SQLException {
        List<AttendanceSession> list = new ArrayList<>();
        String sql = "SELECT ats.*, cl.name AS class_name, c.name AS subject_name, " +
                     "CONCAT(u.first_name,' ',u.last_name) AS teacher_name " +
                     "FROM attendance_sessions ats " +
                     "JOIN class_courses cc ON ats.class_course_id=cc.id " +
                     "JOIN classes cl ON cc.class_id=cl.id " +
                     "JOIN courses c ON cc.course_id=c.id " +
                     "LEFT JOIN teachers t ON ats.created_by=t.id " +
                     "LEFT JOIN users u ON t.user_id=u.id " +
                     "WHERE cc.teacher_id=? " +
                     (from != null ? "AND ats.date >= ? " : "") +
                     (to   != null ? "AND ats.date <= ? " : "") +
                     "ORDER BY ats.date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setLong(idx++, teacherId);
            if (from != null) ps.setDate(idx++, Date.valueOf(from));
            if (to   != null) ps.setDate(idx,   Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSession(rs));
            }
        }
        return list;
    }

    public List<AttendanceSession> findTodaySessionsByTeacher(long teacherId) throws SQLException {
        List<AttendanceSession> list = new ArrayList<>();
        String sql = "SELECT ats.*, cl.name AS class_name, c.name AS subject_name, " +
                     "CONCAT(u.first_name,' ',u.last_name) AS teacher_name " +
                     "FROM attendance_sessions ats " +
                     "JOIN class_courses cc ON ats.class_course_id=cc.id " +
                     "JOIN classes cl ON cc.class_id=cl.id " +
                     "JOIN courses c ON cc.course_id=c.id " +
                     "LEFT JOIN teachers t ON ats.created_by=t.id " +
                     "LEFT JOIN users u ON t.user_id=u.id " +
                     "WHERE cc.teacher_id=? AND ats.date=CURDATE() ORDER BY ats.created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSession(rs));
            }
        }
        return list;
    }

    // ---- Records ----

    public void saveRecord(long sessionId, long studentId, String status, String remark) throws SQLException {
        String sql = "INSERT INTO attendance_records (session_id, student_id, status, remark) VALUES (?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE status=VALUES(status), remark=VALUES(remark)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            ps.setLong(2, studentId);
            ps.setString(3, status);
            ps.setString(4, remark);
            ps.executeUpdate();
        }
    }

    public void saveRecordsBatch(long sessionId, List<long[]> studentStatuses) throws SQLException {
        String sql = "INSERT INTO attendance_records (session_id, student_id, status, remark) VALUES (?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE status=VALUES(status), remark=VALUES(remark)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                for (long[] row : studentStatuses) {
                    ps.setLong(1, sessionId);
                    ps.setLong(2, row[0]);
                    ps.setString(3, row[1] == 1 ? "PRESENT" : row[1] == 2 ? "LATE" : "ABSENT");
                    ps.setString(4, null);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<AttendanceRecord> findRecordsBySession(long sessionId) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT ar.*, CONCAT(u.first_name,' ',u.last_name) AS student_name, st.student_code " +
                     "FROM attendance_records ar " +
                     "JOIN students st ON ar.student_id=st.id " +
                     "JOIN users u ON st.user_id=u.id " +
                     "WHERE ar.session_id=? ORDER BY u.first_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRecord(rs));
            }
        }
        return list;
    }

    public List<AttendanceRecord> findRecordsByStudent(long studentId) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT ar.*, CONCAT(u.first_name,' ',u.last_name) AS student_name, st.student_code, " +
                     "ats.date AS session_date, c.name AS subject_name, cl.name AS class_name " +
                     "FROM attendance_records ar " +
                     "JOIN students st ON ar.student_id=st.id " +
                     "JOIN users u ON st.user_id=u.id " +
                     "JOIN attendance_sessions ats ON ar.session_id=ats.id " +
                     "JOIN class_courses cc ON ats.class_course_id=cc.id " +
                     "JOIN courses c ON cc.course_id=c.id " +
                     "JOIN classes cl ON cc.class_id=cl.id " +
                     "WHERE ar.student_id=? ORDER BY ats.date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRecordFull(rs));
            }
        }
        return list;
    }

    public double getAttendancePercentage(long studentId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, " +
                     "SUM(CASE WHEN status IN ('PRESENT','LATE') THEN 1 ELSE 0 END) AS present " +
                     "FROM attendance_records WHERE student_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    return total == 0 ? 0 : (present * 100.0 / total);
                }
            }
        }
        return 0;
    }

    public double getAttendancePercentageBySubject(long studentId, long classCourseId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total, " +
                     "SUM(CASE WHEN ar.status IN ('PRESENT','LATE') THEN 1 ELSE 0 END) AS present " +
                     "FROM attendance_records ar " +
                     "JOIN attendance_sessions ats ON ar.session_id=ats.id " +
                     "WHERE ar.student_id=? AND ats.class_course_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, classCourseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    return total == 0 ? 0 : (present * 100.0 / total);
                }
            }
        }
        return 0;
    }

    public List<Object[]> getClassAttendanceSummary(long classCourseId) throws SQLException {
        return getClassAttendanceSummaryInRange(classCourseId, null, null);
    }

    /**
     * Class attendance summary filtered by optional date range.
     * Counts only sessions whose date falls within [from, to].
     */
    public List<Object[]> getClassAttendanceSummaryInRange(
            long classCourseId, LocalDate from, LocalDate to) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String dateFilter = buildDateFilter("ats.date", from, to);
        String sql = "SELECT CONCAT(u.first_name,' ',u.last_name) AS student_name, st.student_code, " +
                     "COUNT(ar.id) AS total, " +
                     "SUM(CASE WHEN ar.status='PRESENT' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN ar.status='ABSENT'  THEN 1 ELSE 0 END) AS absent, " +
                     "SUM(CASE WHEN ar.status='LATE'    THEN 1 ELSE 0 END) AS late, " +
                     "ROUND(SUM(CASE WHEN ar.status IN ('PRESENT','LATE') THEN 1 ELSE 0 END)" +
                     "*100.0/NULLIF(COUNT(ar.id),0),1) AS percentage " +
                     "FROM students st " +
                     "JOIN users u ON st.user_id=u.id " +
                     "JOIN enrollments e ON e.student_id=st.id " +
                     "JOIN class_courses cc ON cc.class_id=e.class_id " +
                     "LEFT JOIN attendance_sessions ats ON ats.class_course_id=cc.id " + dateFilter +
                     "LEFT JOIN attendance_records ar ON ar.session_id=ats.id AND ar.student_id=st.id " +
                     "WHERE cc.id=? " +
                     "GROUP BY st.id, u.first_name, u.last_name, st.student_code " +
                     "ORDER BY u.first_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (from != null) ps.setDate(idx++, Date.valueOf(from));
            if (to   != null) ps.setDate(idx++, Date.valueOf(to));
            ps.setLong(idx, classCourseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("student_name"), rs.getString("student_code"),
                        rs.getInt("total"), rs.getInt("present"),
                        rs.getInt("absent"), rs.getInt("late"), rs.getDouble("percentage")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getAllStudentsAttendanceSummary() throws SQLException {
        return getAllStudentsAttendanceSummaryInRange(null, null);
    }

    /**
     * All-students summary filtered by optional date range.
     */
    public List<Object[]> getAllStudentsAttendanceSummaryInRange(
            LocalDate from, LocalDate to) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String dateFilter = buildDateFilter("ats.date", from, to);
        String sql = "SELECT CONCAT(u.first_name,' ',u.last_name) AS student_name, st.student_code, " +
                     "COUNT(ar.id) AS total, " +
                     "SUM(CASE WHEN ar.status='PRESENT' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN ar.status='ABSENT'  THEN 1 ELSE 0 END) AS absent, " +
                     "SUM(CASE WHEN ar.status='LATE'    THEN 1 ELSE 0 END) AS late, " +
                     "ROUND(SUM(CASE WHEN ar.status IN ('PRESENT','LATE') THEN 1 ELSE 0 END)" +
                     "*100.0/NULLIF(COUNT(ar.id),0),1) AS percentage " +
                     "FROM students st " +
                     "JOIN users u ON st.user_id=u.id " +
                     "LEFT JOIN attendance_records ar ON ar.student_id=st.id " +
                     "LEFT JOIN attendance_sessions ats ON ar.session_id=ats.id " + dateFilter +
                     "GROUP BY st.id, u.first_name, u.last_name, st.student_code " +
                     "ORDER BY u.first_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (from != null) ps.setDate(idx++, Date.valueOf(from));
            if (to   != null) ps.setDate(idx,   Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("student_name"), rs.getString("student_code"),
                        rs.getInt("total"), rs.getInt("present"),
                        rs.getInt("absent"), rs.getInt("late"), rs.getDouble("percentage")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getLowAttendanceStudents(double threshold) throws SQLException {
        return getLowAttendanceStudentsInRange(threshold, null, null);
    }

    /**
     * Low-attendance students filtered by optional date range.
     */
    public List<Object[]> getLowAttendanceStudentsInRange(
            double threshold, LocalDate from, LocalDate to) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String dateFilter = buildDateFilter("ats.date", from, to);
        // Need to join sessions when date filter is active
        boolean hasDateFilter = from != null || to != null;
        String sql = "SELECT CONCAT(u.first_name,' ',u.last_name) AS student_name, st.student_code, " +
                     "u.email, COUNT(ar.id) AS total, " +
                     "ROUND(SUM(CASE WHEN ar.status IN ('PRESENT','LATE') THEN 1 ELSE 0 END)" +
                     "*100.0/NULLIF(COUNT(ar.id),0),1) AS percentage " +
                     "FROM students st JOIN users u ON st.user_id=u.id " +
                     "LEFT JOIN attendance_records ar ON ar.student_id=st.id " +
                     (hasDateFilter
                         ? "LEFT JOIN attendance_sessions ats ON ar.session_id=ats.id " + dateFilter
                         : "") +
                     "GROUP BY st.id, u.first_name, u.last_name, st.student_code, u.email " +
                     "HAVING total > 0 AND percentage < ? ORDER BY percentage ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (from != null) ps.setDate(idx++, Date.valueOf(from));
            if (to   != null) ps.setDate(idx++, Date.valueOf(to));
            ps.setDouble(idx, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("student_name"), rs.getString("student_code"),
                        rs.getString("email"), rs.getInt("total"), rs.getDouble("percentage")
                    });
                }
            }
        }
        return list;
    }

    /** Builds a WHERE/AND clause fragment for date filtering on a given column. */
    private String buildDateFilter(String col, LocalDate from, LocalDate to) {
        if (from == null && to == null) return "";
        StringBuilder sb = new StringBuilder();
        if (from != null) sb.append("AND ").append(col).append(" >= ? ");
        if (to   != null) sb.append("AND ").append(col).append(" <= ? ");
        return sb.toString();
    }

    public List<Object[]> getStudentSubjectBreakdown(long studentId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT c.name AS subject_name, c.code AS subject_code, " +
                     "COUNT(ar.id) AS total, " +
                     "SUM(CASE WHEN ar.status='PRESENT' THEN 1 ELSE 0 END) AS present, " +
                     "SUM(CASE WHEN ar.status='ABSENT'  THEN 1 ELSE 0 END) AS absent, " +
                     "SUM(CASE WHEN ar.status='LATE'    THEN 1 ELSE 0 END) AS late, " +
                     "ROUND(SUM(CASE WHEN ar.status IN ('PRESENT','LATE') THEN 1 ELSE 0 END)" +
                     "*100.0/NULLIF(COUNT(ar.id),0),1) AS percentage " +
                     "FROM attendance_records ar " +
                     "JOIN attendance_sessions ats ON ar.session_id=ats.id " +
                     "JOIN class_courses cc ON ats.class_course_id=cc.id " +
                     "JOIN courses c ON cc.course_id=c.id " +
                     "WHERE ar.student_id=? " +
                     "GROUP BY c.id, c.name, c.code ORDER BY c.name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("subject_name"),
                        rs.getString("subject_code"),
                        rs.getInt("total"),
                        rs.getInt("present"),
                        rs.getInt("absent"),
                        rs.getInt("late"),
                        rs.getDouble("percentage")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getMonthlyAttendance(long studentId, int year, int month) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT ats.date, ar.status, c.name AS subject_name " +
                     "FROM attendance_records ar " +
                     "JOIN attendance_sessions ats ON ar.session_id=ats.id " +
                     "JOIN class_courses cc ON ats.class_course_id=cc.id " +
                     "JOIN courses c ON cc.course_id=c.id " +
                     "WHERE ar.student_id=? AND YEAR(ats.date)=? AND MONTH(ats.date)=? " +
                     "ORDER BY ats.date";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getDate("date").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("subject_name")
                    });
                }
            }
        }
        return list;
    }

    /**
     * Updates a single attendance record's status and remark.
     */
    public void updateRecord(long recordId, String status, String remark) throws SQLException {
        String sql = "UPDATE attendance_records SET status=?, remark=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, remark == null || remark.isEmpty() ? null : remark);
            ps.setLong(3, recordId);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes an attendance session and all its records (cascade via FK).
     * Only deletes if the session was created by the given teacher — prevents
     * one teacher from deleting another teacher's sessions.
     *
     * @return true if a row was deleted, false if not found or not owned by teacher
     */
    public boolean deleteSession(long sessionId, long teacherId) throws SQLException {
        String sql = "DELETE FROM attendance_sessions WHERE id=? AND created_by=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            ps.setLong(2, teacherId);
            return ps.executeUpdate() > 0;
        }
    }

    // ---- Mappers ----

    private AttendanceSession mapSession(ResultSet rs) throws SQLException {
        AttendanceSession s = new AttendanceSession();
        s.setId(rs.getLong("id"));
        s.setClassCourseId(rs.getLong("class_course_id"));
        s.setDate(rs.getDate("date").toLocalDate());
        s.setCreatedBy(rs.getLong("created_by"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) s.setCreatedAt(ca.toLocalDateTime());
        s.setClassName(rs.getString("class_name"));
        s.setSubjectName(rs.getString("subject_name"));
        s.setTeacherName(rs.getString("teacher_name"));
        return s;
    }

    private AttendanceRecord mapRecord(ResultSet rs) throws SQLException {
        AttendanceRecord r = new AttendanceRecord();
        r.setId(rs.getLong("id"));
        r.setSessionId(rs.getLong("session_id"));
        r.setStudentId(rs.getLong("student_id"));
        r.setStatus(rs.getString("status"));
        r.setRemark(rs.getString("remark"));
        r.setStudentName(rs.getString("student_name"));
        r.setStudentCode(rs.getString("student_code"));
        return r;
    }

    private AttendanceRecord mapRecordFull(ResultSet rs) throws SQLException {
        AttendanceRecord r = mapRecord(rs);
        Date d = rs.getDate("session_date");
        if (d != null) r.setSessionDate(d.toLocalDate());
        r.setSubjectName(rs.getString("subject_name"));
        r.setClassName(rs.getString("class_name"));
        return r;
    }
}
