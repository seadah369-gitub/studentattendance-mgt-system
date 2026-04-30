package com.attendance.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

/**
 * Manages light/dark theme switching using FlatLaf.
 * Persists the user's preference to a local file so it survives restarts.
 */
public class ThemeManager {

    private static final String PREFS_FILE = "theme.properties";
    private static final String KEY        = "dark_mode";

    private static boolean darkMode = false;

    private ThemeManager() {}

    /** Load saved preference and apply it. Call once at startup before any UI is shown. */
    public static void init() {
        darkMode = loadPreference();
        applyTheme(false); // no repaint needed — nothing is showing yet
    }

    /** Returns true if dark mode is currently active. */
    public static boolean isDark() { return darkMode; }

    /**
     * Toggles between light and dark, repaints all open windows.
     * Call from the EDT.
     */
    public static void toggle() {
        darkMode = !darkMode;
        savePreference(darkMode);
        applyTheme(true);
    }

    // ---- Internal ----

    private static void applyTheme(boolean repaintWindows) {
        try {
            if (darkMode) FlatDarkLaf.setup();
            else          FlatLightLaf.setup();

            // Re-apply custom UI tweaks
            UIManager.put("Button.arc",        6);
            UIManager.put("Component.arc",     6);
            UIManager.put("TextComponent.arc", 6);

            if (repaintWindows) {
                for (Window w : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(w);
                    w.repaint();
                }
            }
        } catch (Exception ignored) {}
    }

    private static boolean loadPreference() {
        File f = new File(PREFS_FILE);
        if (!f.exists()) return false;
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            p.load(fis);
            return "true".equalsIgnoreCase(p.getProperty(KEY, "false"));
        } catch (IOException e) {
            return false;
        }
    }

    private static void savePreference(boolean dark) {
        Properties p = new Properties();
        p.setProperty(KEY, String.valueOf(dark));
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) {
            p.store(fos, null);
        } catch (IOException ignored) {}
    }
}
