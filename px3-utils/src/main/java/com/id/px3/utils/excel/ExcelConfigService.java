package com.id.px3.utils.excel;


import ch.qos.logback.core.util.FileUtil;
import com.id.px3.utils.FileStreamHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ExcelConfigService {

    private final Map<String, ExcelFile> configMap = new HashMap<>();

    /**
     * Load an Excel file and return raw data from a specific sheet
     *
     * @param fileName - path to the file
     * @param sheetName - name of the sheet
     * @return list of maps, each list item is a 'row' of the excel sheet
     */
    public synchronized List<Map<String, Object>> load(String fileName, String sheetName) {
        try(InputStream fis = FileStreamHelper.openFileOrResource(fileName)) {

            //  get absolute path
            String pathToFile = new File(fileName).getAbsolutePath();

            try {
                //  ensure do not load twice the same file
                ExcelFile excelFile = configMap.computeIfAbsent(pathToFile, s -> loadFromStream(fis));

                //  load sheet and return raw data
                return excelFile.getSheet(sheetName).map(s -> s.readAsTable(0, 1, null)).orElse(null);
            } catch (Exception e) {
                throw new RuntimeException("File '%s' could not be loaded: %s".formatted(fileName, e.getMessage()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ExcelFile loadFromStream(InputStream fis) {
        try {
            return ExcelFile.fromStream(fis);
        } catch (IOException e) {
            throw new RuntimeException("FileInputStream could not be loaded");
        }
    }
}
