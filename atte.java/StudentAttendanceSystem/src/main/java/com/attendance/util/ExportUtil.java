package com.attendance.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ExportUtil {

    /**
     * Export visible columns only (skips hidden columns where width == 0).
     * Uses UTF-8 encoding.
     */
    public static void exportTableToCSV(JTable table, File file) throws IOException {
        if (table == null) throw new IOException("Table is not initialized.");
        TableModel model = table.getModel();
        int[] visibleCols = getVisibleColumns(table);

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(ensureExtension(file, "csv")),
                        StandardCharsets.UTF_8))) {
            // BOM for Excel UTF-8 compatibility
            pw.print('\uFEFF');
            // Header
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < visibleCols.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(escape(model.getColumnName(visibleCols[i])));
            }
            pw.println(sb);
            // Rows — use model row count (exports all rows, not just filtered)
            for (int r = 0; r < model.getRowCount(); r++) {
                sb = new StringBuilder();
                for (int i = 0; i < visibleCols.length; i++) {
                    if (i > 0) sb.append(",");
                    Object val = model.getValueAt(r, visibleCols[i]);
                    sb.append(escape(val != null ? val.toString() : ""));
                }
                pw.println(sb);
            }
        }
    }

    /**
     * Export visible columns only with proper numeric cell types so Excel
     * can sort and sum numbers correctly.
     */
    public static void exportTableToExcel(JTable table, String sheetTitle, File file) throws IOException {
        if (table == null) throw new IOException("Table is not initialized.");
        TableModel model = table.getModel();
        int[] visibleCols = getVisibleColumns(table);
        File outFile = ensureExtension(file, "xlsx");

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetTitle);

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);

            // Alternating row styles
            CellStyle evenStyle = wb.createCellStyle();
            CellStyle oddStyle  = wb.createCellStyle();
            oddStyle.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
            oddStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Number format for numeric cells
            CellStyle numStyle     = wb.createCellStyle();
            CellStyle numOddStyle  = wb.createCellStyle();
            DataFormat fmt = wb.createDataFormat();
            numStyle.setDataFormat(fmt.getFormat("0.0"));
            numOddStyle.setDataFormat(fmt.getFormat("0.0"));
            numOddStyle.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
            numOddStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < visibleCols.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(model.getColumnName(visibleCols[i]));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int r = 0; r < model.getRowCount(); r++) {
                Row row = sheet.createRow(r + 1);
                boolean odd = (r % 2 != 0);
                for (int i = 0; i < visibleCols.length; i++) {
                    Object val = model.getValueAt(r, visibleCols[i]);
                    Cell cell = row.createCell(i);
                    if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                        cell.setCellStyle(odd ? numOddStyle : numStyle);
                    } else {
                        cell.setCellValue(val != null ? val.toString() : "");
                        cell.setCellStyle(odd ? oddStyle : evenStyle);
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < visibleCols.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                wb.write(fos);
            }
        }
    }

    /**
     * Export visible columns only to a landscape PDF with title, styled header,
     * alternating row colors, and proper resource cleanup.
     */
    public static void exportTableToPDF(JTable table, String title, File file) throws Exception {
        if (table == null) throw new Exception("Table is not initialized.");
        TableModel model = table.getModel();
        int[] visibleCols = getVisibleColumns(table);
        File outFile = ensureExtension(file, "pdf");

        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outFile));
        try {
            doc.open();

            // Title
            com.itextpdf.text.Font titleFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(33, 33, 33));
            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setSpacingAfter(10);
            doc.add(titlePara);

            // Timestamp
            com.itextpdf.text.Font smallFont =
                    FontFactory.getFont(FontFactory.HELVETICA, 8, new BaseColor(117, 117, 117));
            doc.add(new Paragraph("Generated: " + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), smallFont));
            doc.add(Chunk.NEWLINE);

            // Table
            PdfPTable pdfTable = new PdfPTable(visibleCols.length);
            pdfTable.setWidthPercentage(100);
            pdfTable.setSpacingBefore(4);

            // Column widths — equal by default
            float[] widths = new float[visibleCols.length];
            for (int i = 0; i < widths.length; i++) widths[i] = 1f;
            pdfTable.setWidths(widths);

            // Header row
            com.itextpdf.text.Font hFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
            for (int i = 0; i < visibleCols.length; i++) {
                PdfPCell cell = new PdfPCell(new Phrase(model.getColumnName(visibleCols[i]), hFont));
                cell.setBackgroundColor(new BaseColor(63, 81, 181));
                cell.setPadding(6);
                cell.setBorderColor(new BaseColor(50, 65, 160));
                pdfTable.addCell(cell);
            }

            // Data rows
            com.itextpdf.text.Font dFont =
                    FontFactory.getFont(FontFactory.HELVETICA, 8, new BaseColor(33, 33, 33));
            BaseColor evenBg = new BaseColor(255, 255, 255);
            BaseColor oddBg  = new BaseColor(240, 242, 255);
            BaseColor borderColor = new BaseColor(210, 210, 225);

            for (int r = 0; r < model.getRowCount(); r++) {
                BaseColor bg = (r % 2 == 0) ? evenBg : oddBg;
                for (int i = 0; i < visibleCols.length; i++) {
                    Object val = model.getValueAt(r, visibleCols[i]);
                    String text = val != null ? val.toString() : "";
                    PdfPCell cell = new PdfPCell(new Phrase(text, dFont));
                    cell.setBackgroundColor(bg);
                    cell.setPadding(5);
                    cell.setBorderColor(borderColor);
                    pdfTable.addCell(cell);
                }
            }

            doc.add(pdfTable);

            // Footer with row count
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Total records: " + model.getRowCount(), smallFont));

        } finally {
            if (doc.isOpen()) doc.close();
            writer.close();
        }
    }

    // ---- Helpers ----

    /**
     * Returns model column indices for all columns that are visible (width > 0).
     * Maps view columns back to model columns to handle reordering.
     */
    private static int[] getVisibleColumns(JTable table) {
        java.util.List<Integer> cols = new java.util.ArrayList<>();
        for (int viewCol = 0; viewCol < table.getColumnCount(); viewCol++) {
            if (table.getColumnModel().getColumn(viewCol).getWidth() > 0) {
                cols.add(table.convertColumnIndexToModel(viewCol));
            }
        }
        return cols.stream().mapToInt(Integer::intValue).toArray();
    }

    /** Appends the given extension if the file doesn't already have it. */
    private static File ensureExtension(File file, String ext) {
        String name = file.getName();
        if (!name.toLowerCase().endsWith("." + ext)) {
            return new File(file.getParentFile(), name + "." + ext);
        }
        return file;
    }

    private static String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
