package com.id.px3.utils.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
public class ExcelFile {

    private final XSSFWorkbook workbook;

    private ExcelFile(InputStream inputStream) throws IOException {
        this.workbook = new XSSFWorkbook(inputStream);
    }

    public static ExcelFile fromStream(InputStream inputStream) throws IOException {
        return new ExcelFile(inputStream);
    }

    public Optional<ExcelSheet> getSheet(String sheetName) {
        try {
            if (sheetName == null || sheetName.isBlank()) {
                return Optional.of(new ExcelSheet(workbook.getSheetAt(0)));
            } else {
                return Optional.of(new ExcelSheet(workbook.getSheet(sheetName)));
            }
        } catch (Exception e) {
            log.error(String.format("Error getting sheet '%s'", sheetName != null ? sheetName : "*NULL*"), e);
            return Optional.empty();
        }
    }
}
