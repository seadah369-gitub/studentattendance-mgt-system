package com.attendance.ui.common;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;
import com.attendance.service.AuthService;
import com.attendance.util.UIUtil;
import com.attendance.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Self-service Change Password panel — used by all three roles.
 * Verifies the current password before allowing the update.
 */
public class ChangePasswordPanel extends JPanel {

    private final User user;
    private final AuthService auth   = AuthService.getInstance();
    private final UserDAO userDAO    = new UserDAO();

    private final JPasswordField currentField  = UIUtil.styledPasswordField(24);
    private final JPasswordField newField      = UIUtil.styledPasswordField(24);
    private final JPasswordField confirmField  = UIUtil.styledPasswordField(24);
    private final JLabel         feedbackLabel = new JLabel(" ");

    public ChangePasswordPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(32, 36, 32, 36));

        // ---- Header ----
        JLabel title = UIUtil.headerLabel("Change Password");
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        JLabel sub = UIUtil.label("Update your login password. You must enter your current password first.");
        sub.setForeground(UIUtil.TEXT_SECONDARY);
        sub.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.add(title);
        header.add(sub);
        add(header, BorderLayout.NORTH);

        // ---- Form card ----
        JPanel card = UIUtil.card(null);
        card.setMaximumSize(new Dimension(520, 999));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 230)),
            new EmptyBorder(28, 32, 28, 32)));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Show/hide toggles
        JButton toggleCurrent = toggleBtn();
        JButton toggleNew     = toggleBtn();
        JButton toggleConfirm = toggleBtn();

        form.add(fieldRow("Current Password", currentField, toggleCurrent));
        form.add(Box.createVerticalStrut(16));
        form.add(fieldRow("New Password", newField, toggleNew));
        form.add(hint("Minimum 6 characters"));
        form.add(Box.createVerticalStrut(16));
        form.add(fieldRow("Confirm New Password", confirmField, toggleConfirm));
        form.add(Box.createVerticalStrut(8));

        // Strength bar
        JProgressBar strengthBar = new JProgressBar(0, 4);
        strengthBar.setStringPainted(false);
        strengthBar.setPreferredSize(new Dimension(0, 6));
        strengthBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        strengthBar.setBorderPainted(false);
        strengthBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel strengthLabel = new JLabel(" ");
        strengthLabel.setFont(UIUtil.FONT_SMALL);
        strengthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(strengthBar);
        form.add(Box.createVerticalStrut(2));
        form.add(strengthLabel);
        form.add(Box.createVerticalStrut(20));

        // Feedback label
        feedbackLabel.setFont(UIUtil.FONT_SMALL);
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(feedbackLabel);
        form.add(Box.createVerticalStrut(20));

        // Buttons
        JButton saveBtn  = UIUtil.primaryButton("🔒  Update Password");
        JButton clearBtn = UIUtil.secondaryButton("Clear");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(saveBtn);
        btnRow.add(clearBtn);
        form.add(btnRow);

        card.add(form, BorderLayout.CENTER);

        // Center the card
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        // ---- Wire show/hide toggles ----
        wireToggle(toggleCurrent, currentField);
        wireToggle(toggleNew,     newField);
        wireToggle(toggleConfirm, confirmField);

        // ---- Live password strength ----
        newField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateStrength(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateStrength(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }

            private void updateStrength() {
                String pw = new String(newField.getPassword());
                int score = passwordScore(pw);
                strengthBar.setValue(score);
                switch (score) {
                    case 0 -> { strengthBar.setForeground(UIUtil.BG);      strengthLabel.setText(" "); }
                    case 1 -> { strengthBar.setForeground(UIUtil.DANGER);  strengthLabel.setText("Weak"); strengthLabel.setForeground(UIUtil.DANGER); }
                    case 2 -> { strengthBar.setForeground(UIUtil.WARNING); strengthLabel.setText("Fair"); strengthLabel.setForeground(UIUtil.WARNING); }
                    case 3 -> { strengthBar.setForeground(new Color(100,180,100)); strengthLabel.setText("Good"); strengthLabel.setForeground(new Color(80,150,80)); }
                    case 4 -> { strengthBar.setForeground(UIUtil.SUCCESS); strengthLabel.setText("Strong"); strengthLabel.setForeground(UIUtil.SUCCESS); }
                }
            }
        });

        // ---- Save ----
        saveBtn.addActionListener(e -> doChangePassword());
        clearBtn.addActionListener(e -> {
            currentField.setText("");
            newField.setText("");
            confirmField.setText("");
            feedbackLabel.setText(" ");
            strengthBar.setValue(0);
            strengthLabel.setText(" ");
        });

        // Allow Enter key on confirm field to submit
        confirmField.addActionListener(e -> doChangePassword());
    }

    // ---- Core logic ----

    private void doChangePassword() {
        String current = new String(currentField.getPassword());
        String newPw   = new String(newField.getPassword());
        String confirm = new String(confirmField.getPassword());

        // Validate
        if (current.isEmpty()) { setFeedback("Enter your current password.", false); return; }
        if (!auth.checkPassword(current, user.getPassword())) {
            setFeedback("Current password is incorrect.", false);
            currentField.setText("");
            currentField.requestFocusInWindow();
            return;
        }
        if (!ValidationUtil.isStrongPassword(newPw)) {
            setFeedback("New password must be at least 6 characters.", false);
            return;
        }
        if (newPw.equals(current)) {
            setFeedback("New password must be different from the current password.", false);
            return;
        }
        if (!newPw.equals(confirm)) {
            setFeedback("Passwords do not match.", false);
            confirmField.setText("");
            confirmField.requestFocusInWindow();
            return;
        }

        // Save
        saveBtn: {
            try {
                String hashed = auth.hashPassword(newPw);
                userDAO.updatePassword(user.getId(), hashed);
                // Update in-memory user so checkPassword works for the rest of the session
                user.setPassword(hashed);
                setFeedback("✓ Password updated successfully.", true);
                currentField.setText("");
                newField.setText("");
                confirmField.setText("");
            } catch (Exception ex) {
                setFeedback("Failed to update password: " + ex.getMessage(), false);
            }
        }
    }

    private void setFeedback(String msg, boolean success) {
        feedbackLabel.setText(msg);
        feedbackLabel.setForeground(success ? UIUtil.SUCCESS : UIUtil.DANGER);
    }

    // ---- UI helpers ----

    private JPanel fieldRow(String label, JPasswordField field, JButton toggle) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtil.FONT_BOLD);
        lbl.setForeground(UIUtil.TEXT_PRIMARY);

        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.setOpaque(false);
        inputRow.add(field, BorderLayout.CENTER);
        inputRow.add(toggle, BorderLayout.EAST);

        row.add(lbl, BorderLayout.NORTH);
        row.add(inputRow, BorderLayout.CENTER);
        return row;
    }

    private JLabel hint(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIUtil.FONT_SMALL);
        l.setForeground(UIUtil.TEXT_SECONDARY);
        l.setBorder(new EmptyBorder(2, 0, 0, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton toggleBtn() {
        JButton btn = new JButton("Show");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(UIUtil.PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 210)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(52, 36));
        return btn;
    }

    private void wireToggle(JButton btn, JPasswordField field) {
        btn.addActionListener(e -> {
            if (field.getEchoChar() == (char) 0) {
                field.setEchoChar('•');
                btn.setText("Show");
            } else {
                field.setEchoChar((char) 0);
                btn.setText("Hide");
            }
        });
    }

    /** Returns a score 0-4 based on password complexity. */
    private int passwordScore(String pw) {
        if (pw == null || pw.isEmpty()) return 0;
        int score = 0;
        if (pw.length() >= 6)  score++;
        if (pw.length() >= 10) score++;
        if (pw.matches(".*[A-Z].*") && pw.matches(".*[a-z].*")) score++;
        if (pw.matches(".*[0-9].*") || pw.matches(".*[^a-zA-Z0-9].*")) score++;
        return score;
    }
}
