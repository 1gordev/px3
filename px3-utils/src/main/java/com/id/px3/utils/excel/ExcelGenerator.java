package com.id.px3.utils.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelGenerator {

    private final Workbook workbook;

    /**
     * Default constructor: initializes a new empty workbook.
     */
    public ExcelGenerator() {
        this.workbook = new XSSFWorkbook();
    }

    /**
     * Constructor to initialize from an existing Excel file.
     *
     * @param filePath Path to the existing Excel file.
     * @throws IOException If an error occurs while reading the file.
     */
    public ExcelGenerator(String filePath) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            this.workbook = WorkbookFactory.create(inputStream); // Load the workbook
        } catch (IOException e) {
            log.error("Error loading Excel file: {}", filePath, e);
            throw e;
        }
    }

    /**
     * Add a named sheet to the workbook.
     *
     * @param sheetName The name of the sheet to add.
     * @throws IllegalArgumentException If the sheet already exists.
     */
    public void addSheet(String sheetName) {
        if (workbook.getSheet(sheetName) != null) {
            throw new IllegalArgumentException("Sheet already exists: " + sheetName);
        }
        workbook.createSheet(sheetName);
    }

    /**
     * Write data to a sheet starting from a specific row index.
     *
     * @param sheetName      The name of the sheet to write to.
     * @param data           The data to write as a list of maps.
     *                       Each map represents a row, where the key is the column header and the value is the cell content.
     * @param startRowIndex  The row index to start writing from (0-based).
     * @param isHeaderBold   Whether to make the first row (headers) bold.
     * @throws IllegalArgumentException If the sheet does not exist.
     */
    public void writeToSheet(String sheetName, List<Map<String, Object>> data, int startRowIndex, boolean isHeaderBold) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found: " + sheetName);
        }

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }

        // Get the header row map
        Map<String, Object> headerMap = data.get(0);

        // Create a bold font for the header if required
        CellStyle headerStyle = workbook.createCellStyle();
        if (isHeaderBold) {
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);
        }

        // Write the header row (only if starting at row 0)
        int currentRowIndex = startRowIndex;
        if (currentRowIndex == 0) {
            Row headerRow = sheet.createRow(currentRowIndex++);
            int colIndex = 0;
            for (String key : headerMap.keySet()) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(key); // Set the header name
                if (isHeaderBold) {
                    cell.setCellStyle(headerStyle);
                }
            }
        }

        // Write the data rows
        for (Map<String, Object> rowData : data) {
            Row row = sheet.getRow(currentRowIndex);
            if (row == null) {
                row = sheet.createRow(currentRowIndex);
            }
            int colIndex = 0;

            for (String key : headerMap.keySet()) {
                Cell cell = row.getCell(colIndex);
                if (cell == null) {
                    cell = row.createCell(colIndex);
                }
                Object value = rowData.get(key);

                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                } else if (value != null) {
                    cell.setCellValue(value.toString());
                }
                colIndex++;
            }
            currentRowIndex++;
        }

        // Auto-size columns
        for (int i = 0; i < headerMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Delete a row from the first sheet.
     *
     * @param rowIndex Index of the row to delete (0-based).
     */
    public void deleteRow(int rowIndex) {
        deleteRow(null, rowIndex);
    }

    /**
     * Delete a row from the specified sheet.
     *
     * @param sheetName Name of the sheet (null for the first sheet).
     * @param rowIndex  Index of the row to delete (0-based).
     */
    public void deleteRow(String sheetName, int rowIndex) {
        Sheet sheet = sheetName != null ? workbook.getSheet(sheetName) : workbook.getSheetAt(0);
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found: " + sheetName);
        }

        Row rowToDelete = sheet.getRow(rowIndex);
        if (rowToDelete != null) {
            sheet.removeRow(rowToDelete);

            // Shift rows up to fill the gap if necessary
            if (rowIndex < sheet.getLastRowNum()) {
                sheet.shiftRows(rowIndex + 1, sheet.getLastRowNum(), -1);
            }
        }
    }

    /**
     * Save the workbook to a file.
     *
     * @param fileName The name of the file to save.
     * @throws IOException If an error occurs during file writing.
     */
    public void saveToFile(String fileName) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            log.error("Error saving Excel file", e);
            throw e;
        } finally {
            workbook.close();
        }
    }

    /**
     * Write the workbook to a ByteArrayOutputStream.
     *
     * @return A ByteArrayOutputStream containing the Excel data.
     * @throws IOException If an error occurs during writing.
     */
    public ByteArrayOutputStream writeToStream() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream;
        } catch (IOException e) {
            log.error("Error writing Excel to stream", e);
            throw e;
        } finally {
            workbook.close();
        }
    }

    /**
     * Write a row to the first sheet.
     *
     * @param row The row data to write.
     */
    public void writeRow(List<String> row) {
        writeRow(null, row);
    }

    /**
     * Write a row to a specific sheet.
     *
     * @param sheetName The name of the sheet to write to.
     * @param row The row data to write.
     */
    public void writeRow(String sheetName, List<String> row) {
        Sheet sheet = sheetName != null ? workbook.getSheet(sheetName) : workbook.getSheetAt(0);
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found: " + sheetName);
        }

        Row newRow = sheet.createRow(sheet.getLastRowNum() + 1);
        for (int i = 0; i < row.size(); i++) {
            Cell cell = newRow.createCell(i);
            cell.setCellValue(row.get(i));
        }
    }
}
