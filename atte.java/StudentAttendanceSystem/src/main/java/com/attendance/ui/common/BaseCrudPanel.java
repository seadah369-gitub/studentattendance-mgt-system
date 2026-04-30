package com.attendance.ui.common;

import com.attendance.util.ExportUtil;
import com.attendance.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;

public abstract class BaseCrudPanel extends JPanel {

    protected JTable table;
    protected DefaultTableModel tableModel;
    protected JTextField searchField;
    protected JComboBox<String> filterCombo; // column filter
    protected JButton addBtn, editBtn, deleteBtn, exportCsvBtn, exportXlsBtn;
    protected JLabel rowCountLabel;
    private TableRowSorter<DefaultTableModel> sorter;
    private String[] columnNames;

    public BaseCrudPanel(String title, String[] columns) {
        this.columnNames = columns;
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtil.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── Header ──
        JLabel titleLbl = UIUtil.headerLabel(title);
        titleLbl.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(titleLbl, BorderLayout.NORTH);

        // ── Table (created first so exportBtn can reference it) ──
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtil.styleTable(table);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Row sorter for live filtering
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // ── Toolbar ──
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Search + filter row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchRow.setOpaque(false);

        searchField = UIUtil.styledField(22);
        searchField.setPreferredSize(new Dimension(220, 32));
        searchField.putClientProperty("JTextField.placeholderText", "Search...");

        // Column filter combo — "ALL" + each column name
        filterCombo = new JComboBox<>();
        filterCombo.setFont(UIUtil.FONT_BODY);
        filterCombo.setPreferredSize(new Dimension(150, 32));
        filterCombo.addItem("ALL columns");
        for (int i = 1; i < columns.length; i++) filterCombo.addItem(columns[i]);

        JButton clearBtn = UIUtil.secondaryButton("✕");
        clearBtn.setPreferredSize(new Dimension(34, 32));
        clearBtn.setToolTipText("Clear search");

        rowCountLabel = new JLabel("0 records");
        rowCountLabel.setFont(UIUtil.FONT_SMALL);
        rowCountLabel.setForeground(UIUtil.TEXT_SECONDARY);

        searchRow.add(UIUtil.boldLabel("Search:"));
        searchRow.add(searchField);
        searchRow.add(UIUtil.label("in"));
        searchRow.add(filterCombo);
        searchRow.add(clearBtn);
        searchRow.add(Box.createHorizontalStrut(10));
        searchRow.add(rowCountLabel);

        // Button row — exportBtn now created AFTER table is initialized
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);

        addBtn       = UIUtil.primaryButton("+ Add");
        editBtn      = UIUtil.secondaryButton("✏ Edit");
        deleteBtn    = UIUtil.dangerButton("🗑 Delete");
        exportCsvBtn = UIUtil.secondaryButton("CSV"); // kept for API compat
        exportXlsBtn = UIUtil.secondaryButton("Excel");
        JButton exportBtn = UIUtil.exportButton(this, table, "export"); // table is non-null here

        btnPanel.add(exportBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(addBtn);
        extraButtons(btnPanel);

        toolbar.add(searchRow, BorderLayout.WEST);
        toolbar.add(btnPanel, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(UIUtil.scrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ── Wire actions ──
        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> {
            if (table.getSelectedRow() < 0) { UIUtil.showError(this, "Select a row first."); return; }
            openEditDialog();
        });
        deleteBtn.addActionListener(e -> {
            if (table.getSelectedRow() < 0) { UIUtil.showError(this, "Select a row first."); return; }
            if (UIUtil.confirm(this, "Delete selected record?")) deleteSelected();
        });
        exportCsvBtn.addActionListener(e -> { /* handled by exportBtn popup */ });
        exportXlsBtn.addActionListener(e -> { /* handled by exportBtn popup */ });
        // exportBtn popup is wired inside UIUtil.exportButton()
        clearBtn.addActionListener(e -> { searchField.setText(""); applyFilter(""); });

        // Live search — filter as user types
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(searchField.getText()); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(searchField.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
        });

        // Re-apply filter when column selection changes
        filterCombo.addActionListener(e -> applyFilter(searchField.getText()));

        SwingUtilities.invokeLater(this::loadData);
    }

    private void applyFilter(String text) {
        if (text == null || text.isBlank()) {
            sorter.setRowFilter(null);
        } else {
            String regex = "(?i)" + java.util.regex.Pattern.quote(text.trim());
            int selectedCol = filterCombo.getSelectedIndex();
            if (selectedCol <= 0) {
                // ALL columns — search every column except hidden ID (col 0)
                // Build a filter that checks cols 1..n
                int colCount = tableModel.getColumnCount();
                if (colCount <= 1) {
                    sorter.setRowFilter(RowFilter.regexFilter(regex));
                } else {
                    int[] cols = new int[colCount - 1];
                    for (int i = 0; i < cols.length; i++) cols[i] = i + 1;
                    sorter.setRowFilter(RowFilter.regexFilter(regex, cols));
                }
            } else {
                // Specific column: filterCombo index 1 = table column 1, index 2 = table column 2, etc.
                sorter.setRowFilter(RowFilter.regexFilter(regex, selectedCol));
            }
        }
        updateRowCount();
    }

    protected void updateRowCount() {
        int visible = table.getRowCount();
        int total   = tableModel.getRowCount();
        if (visible == total)
            rowCountLabel.setText(total + " record" + (total != 1 ? "s" : ""));
        else
            rowCountLabel.setText(visible + " of " + total + " records");
    }

    protected abstract void loadData();
    protected abstract void openAddDialog();
    protected abstract void openEditDialog();
    protected abstract void deleteSelected();

    protected void extraButtons(JPanel btnPanel) {}

    protected long getSelectedId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return -1;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (long) tableModel.getValueAt(modelRow, 0);
    }

    protected void clearTable() {
        tableModel.setRowCount(0);
        updateRowCount();
    }

    // Called after loadData() to refresh count and re-apply filter
    protected void afterLoad() {
        applyFilter(searchField.getText());
        updateRowCount();
    }

    // Export is handled by UIUtil.exportButton() popup menu
}
