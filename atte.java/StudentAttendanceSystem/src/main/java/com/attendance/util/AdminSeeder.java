package com.attendance.util;

import com.attendance.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;

/** Run once to seed/reset the admin account. */
public class AdminSeeder {
    public static void main(String[] args) throws Exception {
        String email    = "admin@attendance.com";
        String password = "admin123";
        String hash     = BCrypt.hashpw(password, BCrypt.gensalt(10));

        String sql = "INSERT INTO users (first_name, last_name, email, password, role, phone, status) " +
                     "VALUES ('System','Admin',?,?,'ADMIN','0000000000',TRUE) " +
                     "ON DUPLICATE KEY UPDATE password=?, status=TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hash);
            ps.setString(3, hash);
            ps.executeUpdate();
        }
        System.out.println("Admin seeded successfully.");
        System.out.println("Email:    " + email);
        System.out.println("Password: " + password);
        System.out.println("Hash:     " + hash);
    }
}
