package com.attendance.service;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.sql.SQLException;
import java.util.Properties;

public class AuthService {

    private static AuthService instance;
    private User currentUser;
    private final UserDAO userDAO = new UserDAO();
    private static final String PREFS_FILE = "remember.properties";

    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public User login(String email, String password) throws SQLException {
        User user = userDAO.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("No account found with that email.");
        if (!user.isStatus()) throw new IllegalArgumentException("Account is disabled. Contact admin.");
        if (!BCrypt.checkpw(password, user.getPassword()))
            throw new IllegalArgumentException("Incorrect password.");
        currentUser = user;
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() { return currentUser; }

    public boolean isLoggedIn() { return currentUser != null; }

    public String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    public boolean checkPassword(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }

    public void saveRememberMe(String email) {
        Properties p = new Properties();
        p.setProperty("email", email);
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) {
            p.store(fos, null);
        } catch (IOException ignored) {}
    }

    public String loadRememberedEmail() {
        File f = new File(PREFS_FILE);
        if (!f.exists()) return "";
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            p.load(fis);
            return p.getProperty("email", "");
        } catch (IOException e) {
            return "";
        }
    }

    public void clearRememberMe() {
        new File(PREFS_FILE).delete();
    }
}
