package com.attendance.ui.common;

import com.attendance.util.ThemeManager;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SidebarPanel extends JPanel {

    public interface MenuListener {
        void onMenuSelected(String menuId);
    }

    private final List<JButton> menuButtons = new ArrayList<>();
    private JButton activeButton;
    private MenuListener listener;

    public SidebarPanel(String appTitle, String userName, String role) {
        setLayout(new BorderLayout());
        setBackground(UIUtil.SIDEBAR_BG);
        setPreferredSize(new Dimension(220, 0));

        // Top: app branding
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(UIUtil.PRIMARY_DARK);
        top.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel appLbl = new JLabel(appTitle);
        appLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        appLbl.setForeground(Color.WHITE);
        appLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLbl = new JLabel(userName);
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLbl.setForeground(new Color(180, 190, 255));
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLbl = new JLabel(role);
        roleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLbl.setForeground(UIUtil.ACCENT);
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(appLbl);
        top.add(Box.createVerticalStrut(4));
        top.add(userLbl);
        top.add(Box.createVerticalStrut(2));
        top.add(roleLbl);

        // Menu area
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(UIUtil.SIDEBAR_BG);
        menuPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        add(top, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.CENTER);

        // ---- Bottom: dark mode toggle ----
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIUtil.PRIMARY_DARK);
        bottom.setBorder(new EmptyBorder(10, 14, 14, 14));

        JToggleButton darkBtn = new JToggleButton(
            ThemeManager.isDark() ? "☀  Light Mode" : "🌙  Dark Mode");
        darkBtn.setSelected(ThemeManager.isDark());
        darkBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        darkBtn.setForeground(new Color(200, 210, 255));
        darkBtn.setBackground(new Color(50, 60, 100));
        darkBtn.setBorderPainted(false);
        darkBtn.setFocusPainted(false);
        darkBtn.setOpaque(true);
        darkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        darkBtn.setBorder(new EmptyBorder(8, 12, 8, 12));
        darkBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        darkBtn.addActionListener(e -> {
            ThemeManager.toggle();
            darkBtn.setText(ThemeManager.isDark() ? "☀  Light Mode" : "🌙  Dark Mode");
            darkBtn.setSelected(ThemeManager.isDark());
        });

        bottom.add(darkBtn, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // Store ref for addMenu
        putClientProperty("menuPanel", menuPanel);
    }

    public void addSectionLabel(String text) {
        JPanel menuPanel = (JPanel) getClientProperty("menuPanel");
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(120, 130, 170));
        lbl.setBorder(new EmptyBorder(14, 16, 4, 16));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.add(lbl);
    }

    public JButton addMenu(String icon, String label, String menuId) {
        JPanel menuPanel = (JPanel) getClientProperty("menuPanel");
        JButton btn = new JButton(icon + "  " + label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(UIUtil.SIDEBAR_TEXT);
        btn.setBackground(UIUtil.SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(new Color(50, 60, 100));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(UIUtil.SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> {
            setActive(btn);
            if (listener != null) listener.onMenuSelected(menuId);
        });

        menuButtons.add(btn);
        menuPanel.add(btn);
        return btn;
    }

    public void setActive(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(UIUtil.SIDEBAR_BG);
            activeButton.setForeground(UIUtil.SIDEBAR_TEXT);
            activeButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        activeButton = btn;
        btn.setBackground(UIUtil.SIDEBAR_SEL);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    public void setActiveById(String menuId) {
        for (JButton b : menuButtons) {
            // Match button by checking its action listeners for the menuId
            // Buttons fire listener.onMenuSelected(menuId) — we match by text suffix
            if (b.getText().trim().endsWith(menuId) || b.getText().contains(menuId)) {
                setActive(b);
                return;
            }
        }
    }

    public void setMenuListener(MenuListener listener) {
        this.listener = listener;
    }

    public void selectFirst() {
        if (!menuButtons.isEmpty()) menuButtons.get(0).doClick();
    }
}
