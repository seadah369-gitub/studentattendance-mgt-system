package com.attendance.config;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DatabaseConfig {

    private static final String CONFIG_FILE = "db.properties";
    private static String url;
    private static String username;
    private static String password;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                setDefaults(props);
            }
        } else {
            setDefaults(props);
            saveConfig(props);
        }
        url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/attendance_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        username = props.getProperty("db.username", "root");
        password = props.getProperty("db.password", "");
    }

    private static void setDefaults(Properties props) {
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/attendance_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        props.setProperty("db.username", "root");
        props.setProperty("db.password", "");
    }

    private static void saveConfig(Properties props) {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Database Configuration");
        } catch (IOException ignored) {}
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void updateConfig(String dbUrl, String dbUser, String dbPass) {
        url = dbUrl;
        username = dbUser;
        password = dbPass;
        Properties props = new Properties();
        props.setProperty("db.url", dbUrl);
        props.setProperty("db.username", dbUser);
        props.setProperty("db.password", dbPass);
        saveConfig(props);
    }

    public static String getUrl() { return url; }
    public static String getUsername() { return username; }
}
