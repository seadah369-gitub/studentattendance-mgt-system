 package com.attendance.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIUtil {

    // Icon cache
    private static final java.util.Map<String, ImageIcon> iconCache = new java.util.HashMap<>();

    /** Loads an icon from resources with caching. */
    public static ImageIcon getIcon(String name, int size) {
        String key = name + "_" + size;
        if (iconCache.containsKey(key)) {
            return iconCache.get(key);
        }
        try {
            java.net.URL url = UIUtil.class.getResource("/" + name + ".png");
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
                iconCache.put(key, icon);
                return icon;
            }
        } catch (Exception e) {
            System.err.println("Icon not found: " + name);
        }
        return null;
    }

    // Color palette
    public static final Color PRIMARY       = new Color(63, 81, 181);
    public static final Color PRIMARY_DARK  = new Color(40, 53, 147);
    public static final Color ACCENT        = new Color(0, 188, 212);
    public static final Color SUCCESS       = new Color(76, 175, 80);
    public static final Color WARNING       = new Color(255, 152, 0);
    public static final Color DANGER        = new Color(244, 67, 54);
    public static final Color BG            = new Color(245, 246, 250);
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color TEXT_PRIMARY  = new Color(33, 33, 33);
    public static final Color TEXT_SECONDARY= new Color(117, 117, 117);
    public static final Color SIDEBAR_BG   = new Color(30, 39, 73);
    public static final Color SIDEBAR_TEXT  = new Color(200, 210, 255);
    public static final Color SIDEBAR_SEL   = new Color(63, 81, 181);

    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);

    public static JButton primaryButton(String text) {
        return primaryButton(text, null);
    }

    public static JButton primaryButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);
        return btn;
    }

    public static JButton dangerButton(String text) {
        return dangerButton(text, null);
    }

    public static JButton dangerButton(String text, Icon icon) {
        JButton btn = primaryButton(text, icon);
        btn.setBackground(DANGER);
        return btn;
    }

    public static JButton successButton(String text) {
        return successButton(text, null);
    }

    public static JButton successButton(String text, Icon icon) {
        JButton btn = primaryButton(text, icon);
        btn.setBackground(SUCCESS);
        return btn;
    }

    public static JButton secondaryButton(String text) {
        return secondaryButton(text, null);
    }

    public static JButton secondaryButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setBackground(new Color(224, 224, 224));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);
        return btn;
    }

    public static JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    public static JPasswordField styledPasswordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    public static JComboBox<?> styledCombo() {
        JComboBox<?> cb = new JComboBox<>();
        cb.setFont(FONT_BODY);
        cb.setBackground(Color.WHITE);
        return cb;
    }

    public static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 230)),
            new EmptyBorder(14, 16, 14, 16)));
        if (title != null && !title.isEmpty()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(FONT_HEADER);
            lbl.setForeground(PRIMARY);
            lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(new Color(230, 230, 240));
        table.setSelectionBackground(new Color(197, 202, 233));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        // Allow sorting by clicking header
        header.setResizingAllowed(true);
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);

        // Alternating row colors — uses view row index (correct for sorted tables)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 255));
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    public static JScrollPane scrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 230)));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    public static JPanel formRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_BODY);
        lbl.setPreferredSize(new Dimension(130, 30));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /**
     * Creates a single "⬇ Export" button that shows a popup menu with
     * CSV / Excel / PDF / Print options when clicked.
     *
     * Usage:
     *   JButton exportBtn = UIUtil.exportButton(parent, table, "report_name");
     *   toolbar.add(exportBtn);
     *
     * @param parent    component used to anchor the popup and show dialogs
     * @param table     the JTable to export
     * @param baseName  default file name without extension (e.g. "attendance_report")
     */
    public static JButton exportButton(Component parent, javax.swing.JTable table, String baseName) {
        JButton btn = secondaryButton("⬇ Export ▾");
        btn.setToolTipText("Export or print table data");

        JPopupMenu menu = new JPopupMenu();

        JMenuItem csvItem   = new JMenuItem("📄  CSV  (.csv)");
        JMenuItem xlsItem   = new JMenuItem("📊  Excel  (.xlsx)");
        JMenuItem pdfItem   = new JMenuItem("📑  PDF  (.pdf)");
        JMenuItem printItem = new JMenuItem("🖨  Print…");

        csvItem.setFont(FONT_BODY);
        xlsItem.setFont(FONT_BODY);
        pdfItem.setFont(FONT_BODY);
        printItem.setFont(FONT_BODY);

        printItem.setAccelerator(
            javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_P,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        menu.add(csvItem);
        menu.add(xlsItem);
        menu.addSeparator();
        menu.add(pdfItem);
        menu.addSeparator();
        menu.add(printItem);

        // Guard: check table is non-null and has data before exporting
        Runnable guardCheck = () -> {
            if (table == null) { showError(parent, "Export failed: table not initialized."); return; }
        };

        csvItem.addActionListener(e -> {
            if (table == null) { showError(parent, "Export failed: table not initialized."); return; }
            if (table.getRowCount() == 0) { showError(parent, "Nothing to export — the table is empty."); return; }
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File(baseName + ".csv"));
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try { ExportUtil.exportTableToCSV(table, fc.getSelectedFile()); showSuccess(parent, "Exported to CSV."); }
                catch (Exception ex) { showError(parent, "Export failed: " + ex.getMessage()); }
            }
        });

        xlsItem.addActionListener(e -> {
            if (table == null) { showError(parent, "Export failed: table not initialized."); return; }
            if (table.getRowCount() == 0) { showError(parent, "Nothing to export — the table is empty."); return; }
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File(baseName + ".xlsx"));
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try { ExportUtil.exportTableToExcel(table, baseName, fc.getSelectedFile()); showSuccess(parent, "Exported to Excel."); }
                catch (Exception ex) { showError(parent, "Export failed: " + ex.getMessage()); }
            }
        });

        pdfItem.addActionListener(e -> {
            if (table == null) { showError(parent, "Export failed: table not initialized."); return; }
            if (table.getRowCount() == 0) { showError(parent, "Nothing to export — the table is empty."); return; }
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File(baseName + ".pdf"));
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try { ExportUtil.exportTableToPDF(table, baseName, fc.getSelectedFile()); showSuccess(parent, "Exported to PDF."); }
                catch (Exception ex) { showError(parent, "Export failed: " + ex.getMessage()); }
            }
        });

        printItem.addActionListener(e -> printTable(parent, table, baseName));

        btn.addActionListener(e -> menu.show(btn, 0, btn.getHeight()));

        if (parent instanceof javax.swing.JComponent jc) {
            jc.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_P,
                    java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                "printTable_" + baseName);
            jc.getActionMap().put("printTable_" + baseName,
                new javax.swing.AbstractAction() {
                    @Override public void actionPerformed(java.awt.event.ActionEvent ev) {
                        printTable(parent, table, baseName);
                    }
                });
        }

        return btn;
    }

    /**
     * Prints the table using Java's built-in JTable.print() which opens the
     * system print dialog. Renders the table with a header (title) and footer
     * (page numbers). Works with any installed printer or "Print to PDF".
     */
    private static void printTable(Component parent, javax.swing.JTable table, String title) {
        if (table.getRowCount() == 0) {
            showError(parent, "Nothing to print — the table is empty.");
            return;
        }
        try {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();

            // Page format: landscape for wide tables
            java.awt.print.PageFormat pf = job.defaultPage();
            pf.setOrientation(java.awt.print.PageFormat.LANDSCAPE);
            pf = job.pageDialog(pf); // let user adjust page settings

            // Header: report title centred; Footer: page N of M right-aligned
            java.text.MessageFormat header = new java.text.MessageFormat(title);
            java.text.MessageFormat footer = new java.text.MessageFormat("Page {0}");

            // JTable.print() returns false if user cancelled
            boolean printed = table.print(
                javax.swing.JTable.PrintMode.FIT_WIDTH,
                header, footer,
                true,   // show print dialog
                null,   // use default print service
                true    // interactive (show progress dialog)
            );

            if (printed) showSuccess(parent, "Document sent to printer.");
            // If cancelled, do nothing silently

        } catch (java.awt.print.PrinterAbortException ignored) {
            // User cancelled — no error needed
        } catch (java.awt.print.PrinterException ex) {
            showError(parent, "Print failed: " + ex.getMessage());
        }
    }

    /** Stat card for dashboards */
    public static JPanel statCard(String title, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(color);
        p.setBorder(new EmptyBorder(18, 20, 18, 20));
        p.setPreferredSize(new Dimension(180, 100));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(Color.WHITE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(255, 255, 255, 200));

        p.add(valLbl, BorderLayout.CENTER);
        p.add(titleLbl, BorderLayout.SOUTH);
        return p;
    }

    // ===== Icon Button Factory Methods =====

    /** Add button with + icon */
    public static JButton addButton(String text) {
        return primaryButton(text, getIcon("add", 16));
    }

    /** Edit button with pencil icon */
    public static JButton editButton(String text) {
        return secondaryButton(text, getIcon("edit", 16));
    }

    /** Delete button with trash icon */
    public static JButton deleteButton(String text) {
        return dangerButton(text, getIcon("delete", 16));
    }

    /** Save button with disk icon */
    public static JButton saveButton(String text) {
        return primaryButton(text, getIcon("save", 16));
    }

    /** Refresh button with refresh icon */
    public static JButton refreshButton(String text) {
        return secondaryButton("↺ " + text, getIcon("edit", 16));
    }

    /** Calendar button with calendar icon */
    public static JButton calendarButton(String text) {
        return secondaryButton(text, getIcon("calander", 16));
    }
}
