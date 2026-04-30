package com.attendance.ui.student;

import com.attendance.dao.UserDAO;
import com.attendance.model.Student;
import com.attendance.model.User;
import com.attendance.util.AvatarUtil;
import com.attendance.util.UIUtil;
import com.attendance.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Allows a student to edit their own email and phone number.
 * Read-only fields (name, student code, gender, DOB) are shown for context.
 */
public class EditProfilePanel extends JPanel {

    private final Student student;
    private final User    user;
    private final UserDAO userDAO = new UserDAO();

    // Editable
    private final JTextField emailField = UIUtil.styledField(28);
    private final JTextField phoneField = UIUtil.styledField(28);

    // Feedback
    private final JLabel feedbackLabel = new JLabel(" ");

    public EditProfilePanel(Student student, User user) {
        this.student = student;
        this.user    = user;

        setLayout(new BorderLayout());
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(32, 36, 32, 36));

        // ---- Header ----
        JLabel title = UIUtil.headerLabel("Edit Profile");
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        JLabel sub = UIUtil.label("Update your contact information. Name and student code can only be changed by an admin.");
        sub.setForeground(UIUtil.TEXT_SECONDARY);
        sub.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.add(title);
        header.add(sub);
        add(header, BorderLayout.NORTH);

        // ---- Card ----
        JPanel card = UIUtil.card(null);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 230)),
            new EmptyBorder(28, 32, 28, 32)));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // ---- Avatar section ----
        JLabel avatarSectionTitle = new JLabel("Profile Photo");
        avatarSectionTitle.setFont(UIUtil.FONT_BOLD);
        avatarSectionTitle.setForeground(UIUtil.TEXT_SECONDARY);
        avatarSectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        avatarSectionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        form.add(avatarSectionTitle);

        String initials = (student.getFirstName() != null ? student.getFirstName().substring(0, 1) : "")
                        + (student.getLastName()  != null ? student.getLastName().substring(0, 1)  : "");

        // Avatar label — clicking opens file chooser
        JLabel avatarLabel = AvatarUtil.makeAvatarLabel(
            student.getId(), 90, initials, UIUtil.PRIMARY,
            () -> feedbackLabel.setText("✓ Photo updated."));

        JButton uploadBtn = UIUtil.secondaryButton("📷  Upload Photo");
        JButton removeBtn = UIUtil.dangerButton("Remove Photo");
        uploadBtn.setFont(UIUtil.FONT_SMALL);
        removeBtn.setFont(UIUtil.FONT_SMALL);
        uploadBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
        removeBtn.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel avatarBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        avatarBtnRow.setOpaque(false);
        avatarBtnRow.add(uploadBtn);
        avatarBtnRow.add(removeBtn);

        JLabel avatarHint = new JLabel("JPG, PNG, GIF up to any size. Will be cropped to a square.");
        avatarHint.setFont(UIUtil.FONT_SMALL);
        avatarHint.setForeground(UIUtil.TEXT_SECONDARY);

        JPanel avatarRight = new JPanel();
        avatarRight.setLayout(new BoxLayout(avatarRight, BoxLayout.Y_AXIS));
        avatarRight.setOpaque(false);
        avatarRight.setBorder(new EmptyBorder(0, 16, 0, 0));
        avatarRight.add(avatarBtnRow);
        avatarRight.add(Box.createVerticalStrut(6));
        avatarRight.add(avatarHint);

        JPanel avatarRow = new JPanel(new BorderLayout());
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        avatarRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        avatarRow.add(avatarLabel,  BorderLayout.WEST);
        avatarRow.add(avatarRight,  BorderLayout.CENTER);
        form.add(avatarRow);
        form.add(Box.createVerticalStrut(20));

        // Upload button action
        uploadBtn.addActionListener(e -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setDialogTitle("Choose Profile Photo");
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", AvatarUtil.acceptedExtensions()));
            if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                try {
                    AvatarUtil.saveAvatar(student.getId(), fc.getSelectedFile());
                    avatarLabel.setIcon(AvatarUtil.loadCircularAvatar(
                        student.getId(), 90, initials, UIUtil.PRIMARY));
                    setFeedback("✓ Photo updated.", true);
                } catch (java.io.IOException ex) {
                    setFeedback("Could not save photo: " + ex.getMessage(), false);
                }
            }
        });

        // Remove button action
        removeBtn.addActionListener(e -> {
            if (!UIUtil.confirm(this, "Remove your profile photo?")) return;
            AvatarUtil.deleteAvatar(student.getId());
            avatarLabel.setIcon(AvatarUtil.initialsIcon(initials, 90, UIUtil.PRIMARY));
            setFeedback("✓ Photo removed.", true);
        });

        // Separator
        JSeparator avatarSep = new JSeparator();
        avatarSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        avatarSep.setForeground(new Color(220, 220, 230));
        avatarSep.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(avatarSep);
        form.add(Box.createVerticalStrut(20));

        // ---- Read-only info section ----
        JLabel roTitle = new JLabel("Account Information");
        roTitle.setFont(UIUtil.FONT_BOLD);
        roTitle.setForeground(UIUtil.TEXT_SECONDARY);
        roTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        roTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        form.add(roTitle);

        JPanel roGrid = new JPanel(new GridLayout(0, 2, 16, 8));
        roGrid.setOpaque(false);
        roGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        roGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));

        addReadOnly(roGrid, "Full Name",     student.getFullName());
        addReadOnly(roGrid, "Student Code",  student.getStudentCode());
        addReadOnly(roGrid, "Gender",        student.getGender() != null ? student.getGender() : "—");
        addReadOnly(roGrid, "Date of Birth", student.getDateOfBirth() != null
                                             ? student.getDateOfBirth().toString() : "—");
        form.add(roGrid);
        form.add(Box.createVerticalStrut(24));

        // ---- Divider ----
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(220, 220, 230));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sep);
        form.add(Box.createVerticalStrut(20));

        // ---- Editable section ----
        JLabel editTitle = new JLabel("Contact Information  (editable)");
        editTitle.setFont(UIUtil.FONT_BOLD);
        editTitle.setForeground(UIUtil.PRIMARY);
        editTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        editTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        form.add(editTitle);

        // Pre-fill
        emailField.setText(student.getEmail() != null ? student.getEmail() : "");
        phoneField.setText(student.getPhone() != null ? student.getPhone() : "");

        form.add(editRow("Email Address *", emailField,
                "Used for login and notifications"));
        form.add(Box.createVerticalStrut(14));
        form.add(editRow("Phone Number", phoneField,
                "Optional — digits, +, - and spaces allowed"));
        form.add(Box.createVerticalStrut(20));

        // Feedback
        feedbackLabel.setFont(UIUtil.FONT_SMALL);
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(feedbackLabel);
        form.add(Box.createVerticalStrut(16));

        // Buttons
        JButton saveBtn   = UIUtil.primaryButton("💾  Save Changes");
        JButton resetBtn  = UIUtil.secondaryButton("Reset");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(saveBtn);
        btnRow.add(resetBtn);
        form.add(btnRow);

        card.add(form, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        // ---- Wire ----
        saveBtn.addActionListener(e -> save());
        resetBtn.addActionListener(e -> {
            emailField.setText(student.getEmail() != null ? student.getEmail() : "");
            phoneField.setText(student.getPhone() != null ? student.getPhone() : "");
            feedbackLabel.setText(" ");
        });
        emailField.addActionListener(e -> phoneField.requestFocusInWindow());
        phoneField.addActionListener(e -> save());
    }

    // ---- Save logic ----

    private void save() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validate
        if (email.isEmpty()) {
            setFeedback("Email address is required.", false);
            emailField.requestFocusInWindow();
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            setFeedback("Please enter a valid email address.", false);
            emailField.requestFocusInWindow();
            return;
        }
        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            setFeedback("Phone number format is invalid (7–15 digits, +, - allowed).", false);
            phoneField.requestFocusInWindow();
            return;
        }

        // Check if email changed and already taken by another user
        if (!email.equalsIgnoreCase(user.getEmail())) {
            try {
                User existing = userDAO.findByEmail(email);
                if (existing != null && existing.getId() != user.getId()) {
                    setFeedback("That email is already used by another account.", false);
                    emailField.requestFocusInWindow();
                    return;
                }
            } catch (Exception ex) {
                setFeedback("Could not verify email: " + ex.getMessage(), false);
                return;
            }
        }

        // Persist
        try {
            user.setEmail(email);
            user.setPhone(phone.isEmpty() ? null : phone);
            userDAO.update(user);

            // Keep in-memory Student model in sync
            student.setEmail(email);
            student.setPhone(phone.isEmpty() ? null : phone);

            setFeedback("✓ Profile updated successfully.", true);
        } catch (Exception ex) {
            setFeedback("Failed to save: " + ex.getMessage(), false);
        }
    }

    // ---- UI helpers ----

    private void setFeedback(String msg, boolean success) {
        feedbackLabel.setText(msg);
        feedbackLabel.setForeground(success ? UIUtil.SUCCESS : UIUtil.DANGER);
    }

    /** A label + read-only value pair added to a GridLayout panel. */
    private void addReadOnly(JPanel grid, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtil.FONT_BOLD);
        lbl.setForeground(UIUtil.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(UIUtil.FONT_BODY);
        val.setForeground(UIUtil.TEXT_PRIMARY);

        grid.add(lbl);
        grid.add(val);
    }

    /** A labeled text field with a hint below it. */
    private JPanel editRow(String label, JTextField field, String hint) {
        JPanel row = new JPanel(new BorderLayout(0, 3));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtil.FONT_BOLD);
        lbl.setForeground(UIUtil.TEXT_PRIMARY);

        JLabel hintLbl = new JLabel(hint);
        hintLbl.setFont(UIUtil.FONT_SMALL);
        hintLbl.setForeground(UIUtil.TEXT_SECONDARY);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(hintLbl, BorderLayout.WEST);

        row.add(lbl,   BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        row.add(south, BorderLayout.SOUTH);
        return row;
    }
}
