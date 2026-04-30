package com.attendance.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Utility for student avatar images.
 *
 * Avatars are stored as JPEG files in the local "avatars/" directory
 * named by student ID: avatars/student_<id>.jpg
 *
 * No DB blobs — keeps the DB lean and loading fast.
 */
public class AvatarUtil {

    private static final String AVATAR_DIR  = "avatars";
    private static final int    AVATAR_SIZE = 96;   // stored size in pixels
    private static final String[] ACCEPTED  = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};

    // ---- Public API ----

    /**
     * Returns the avatar file for a student, or null if none exists.
     */
    public static File getAvatarFile(long studentId) {
        File f = new File(AVATAR_DIR, "student_" + studentId + ".jpg");
        return f.exists() ? f : null;
    }

    /**
     * Saves an image file as the student's avatar.
     * Resizes and crops to a square, then saves as JPEG.
     *
     * @throws IOException if the file cannot be read or written
     */
    public static void saveAvatar(long studentId, File sourceFile) throws IOException {
        BufferedImage src = ImageIO.read(sourceFile);
        if (src == null) throw new IOException("Cannot read image file: " + sourceFile.getName());

        // Crop to square from centre, then scale to AVATAR_SIZE
        BufferedImage square = cropToSquare(src);
        BufferedImage scaled = scale(square, AVATAR_SIZE);

        File dir = new File(AVATAR_DIR);
        if (!dir.exists()) dir.mkdirs();

        File dest = new File(dir, "student_" + studentId + ".jpg");
        ImageIO.write(scaled, "jpg", dest);
    }

    /**
     * Deletes the avatar for a student (if it exists).
     */
    public static void deleteAvatar(long studentId) {
        File f = new File(AVATAR_DIR, "student_" + studentId + ".jpg");
        if (f.exists()) f.delete();
    }

    /**
     * Loads the avatar as a circular ImageIcon scaled to the given diameter.
     * Returns a generated initials avatar if no file exists.
     *
     * @param studentId  student ID
     * @param diameter   display size in pixels
     * @param initials   2-letter fallback (e.g. "AB")
     * @param bgColor    background color for the initials avatar
     */
    public static ImageIcon loadCircularAvatar(long studentId, int diameter,
                                               String initials, Color bgColor) {
        File f = getAvatarFile(studentId);
        if (f != null) {
            try {
                BufferedImage img = ImageIO.read(f);
                if (img != null) return toCircularIcon(img, diameter);
            } catch (IOException ignored) {}
        }
        // Fallback: initials avatar
        return initialsIcon(initials, diameter, bgColor);
    }

    /**
     * Returns accepted image file extensions for a JFileChooser filter.
     */
    public static String[] acceptedExtensions() { return ACCEPTED; }

    // ---- Image processing ----

    private static BufferedImage cropToSquare(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int side = Math.min(w, h);
        int x = (w - side) / 2, y = (h - side) / 2;
        return img.getSubimage(x, y, side, side);
    }

    private static BufferedImage scale(BufferedImage img, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(img, 0, 0, size, size, null);
        g.dispose();
        return out;
    }

    private static ImageIcon toCircularIcon(BufferedImage src, int diameter) {
        BufferedImage scaled = scale(src, diameter);
        BufferedImage out    = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return new ImageIcon(out);
    }

    /**
     * Generates a circular avatar with the student's initials on a colored background.
     */
    public static ImageIcon initialsIcon(String initials, int diameter, Color bg) {
        String text = initials != null && !initials.isBlank()
            ? initials.substring(0, Math.min(2, initials.length())).toUpperCase()
            : "?";

        BufferedImage out = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Circle background
        g.setColor(bg != null ? bg : new Color(63, 81, 181));
        g.fillOval(0, 0, diameter, diameter);

        // Initials text
        g.setColor(Color.WHITE);
        int fontSize = diameter / 3;
        g.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int tx = (diameter - fm.stringWidth(text)) / 2;
        int ty = (diameter - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
        g.dispose();
        return new ImageIcon(out);
    }

    /**
     * Creates a JLabel that displays a circular avatar.
     * Clicking it opens a file chooser to upload a new photo.
     * Pass null for onChanged if you don't need a callback.
     */
    public static JLabel makeAvatarLabel(long studentId, int diameter,
                                         String initials, Color bgColor,
                                         Runnable onChanged) {
        JLabel lbl = new JLabel(loadCircularAvatar(studentId, diameter, initials, bgColor));
        lbl.setPreferredSize(new Dimension(diameter, diameter));
        lbl.setToolTipText("Click to change photo");
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (onChanged != null) {
            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    JFileChooser fc = new JFileChooser();
                    fc.setDialogTitle("Choose Profile Photo");
                    fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Image files (JPG, PNG, GIF, BMP)", acceptedExtensions()));
                    if (fc.showOpenDialog(lbl) == JFileChooser.APPROVE_OPTION) {
                        try {
                            saveAvatar(studentId, fc.getSelectedFile());
                            lbl.setIcon(loadCircularAvatar(studentId, diameter, initials, bgColor));
                            onChanged.run();
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(lbl,
                                "Could not save photo: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
        }
        return lbl;
    }
}
