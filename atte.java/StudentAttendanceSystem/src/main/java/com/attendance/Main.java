package com.attendance;

import com.attendance.config.DatabaseConfig;
import com.attendance.ui.auth.DbSetupDialog;
import com.attendance.ui.auth.LoginFrame;
import com.attendance.util.ThemeManager;

import javax.swing.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        try {
            ThemeManager.init();   // loads saved preference (light or dark)
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
            UIManager.put("TextComponent.arc", 6);
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            if (!DatabaseConfig.testConnection()) {
                DbSetupDialog setup = new DbSetupDialog(null);
                setup.setVisible(true);
                if (!setup.isConnected()) {
                    JOptionPane.showMessageDialog(null,
                        "Cannot start without a database connection.", "Fatal Error",
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
            initSchema();
            new LoginFrame().setVisible(true);
        });
    }

    private static void initSchema() {
        try (InputStream is = Main.class.getResourceAsStream("/schema.sql")) {
            if (is == null) return;
            String sql = new String(is.readAllBytes());
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement st = conn.createStatement()) {
                for (String stmt : sql.split(";")) {
                    String trimmed = stmt.trim();
                    if (!trimmed.isEmpty()) {
                        try { st.execute(trimmed); } catch (Exception ignored) {}
                    }
                }
            }
            autoMigrate();
        } catch (Exception ex) {
            System.err.println("Schema init warning: " + ex.getMessage());
        }
    }

    private static void autoMigrate() {
        String[] fixes = {
            // Fix any courses that ended up with a null/empty code
            "UPDATE courses SET code = CONCAT('CRS', LPAD(id,3,'0')) WHERE code IS NULL OR code = ''",
            // Ensure class_course_id is NOT NULL (restore original constraint if it was relaxed)
            "ALTER TABLE attendance_sessions MODIFY COLUMN class_course_id BIGINT NOT NULL",
            // Drop the old redundant trigger if it exists from a previous version
            "DROP TRIGGER IF EXISTS trg_sync_session",
            // Drop the old mirror tables if they exist from a previous version
            "DROP TABLE IF EXISTS class_subjects",
            "DROP TABLE IF EXISTS subjects"
        };
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            for (String sql : fixes) {
                try { st.execute(sql); } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            System.err.println("Migration warning: " + ex.getMessage());
        }
    }
}
