package com.id.px3.utils.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelGenerator {

    private final Workbook workbook;

    public ExcelGenerator() {
        this.workbook = new XSSFWorkbook(); // Create a new workbook
    }

    /**
     * Add a sheet with data to the workbook.
     *
     * @param sheetName      The name of the sheet.
     * @param data           The data for the sheet as a list of maps.
     *                       Each map represents a row, where the key is the column header and the value is the cell content.
     * @param isHeaderBold   Whether to make the first row (header) bold.
     */
    public void addSheet(String sheetName, List<Map<String, Object>> data, boolean isHeaderBold) {
        Sheet sheet = workbook.createSheet(sheetName); // Create a new sheet

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }

        // Create the header row
        Row headerRow = sheet.createRow(0);
        Map<String, Object> headerMap = data.get(0); // Use the first row's keys as headers
        int colIndex = 0;

        // Create a bold font for the header if required
        CellStyle headerStyle = workbook.createCellStyle();
        if (isHeaderBold) {
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);
        }

        for (String key : headerMap.keySet()) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(key); // Set the header name
            if (isHeaderBold) {
                cell.setCellStyle(headerStyle);
            }
        }

        // Populate the rows with data
        int rowIndex = 1;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            colIndex = 0;
            for (String key : headerMap.keySet()) {
                Cell cell = row.createCell(colIndex++);
                Object value = rowData.get(key);

                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                } else if (value != null) {
                    cell.setCellValue(value.toString());
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < headerMap.size(); i++) {
            sheet.autoSizeColumn(i);
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
        } catch(IOException e) {
            log.error("Error saving Excel file", e);
            throw e;
        } finally {
            workbook.close();
        }
    }
}
