package com.id.px3.utils.excel;

import com.id.px3.utils.ReplaceHelper;
import lombok.Getter;
import org.apache.poi.ss.usermodel.*;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ExcelSheet {

    private final Sheet workbookSheet;

    public ExcelSheet(Sheet workbookSheet) {
        this.workbookSheet = workbookSheet;
    }

    /**
     * Read header row and return it as a list of columns
     *
     * @param headerRowIdx
     * @return
     */
    public List<String> readHeader(int headerRowIdx) {
        return _readHeader(workbookSheet.getRow(headerRowIdx));
    }

    /**
     * Reads the entire section as a table.
     * The section support ${...} tag replacement via optional object replaceMap
     *
     * @param headerRowIdx - 0-based index of the header row; -1 means no header
     * @param dataRowIdx   - 0-based index of the first data row
     * @param replaceMap   - replaceMap [optional]
     * @return
     */
    public List<Map<String, Object>> readAsTable(int headerRowIdx, int dataRowIdx, Map<String, String> replaceMap) {
        List<Map<String, Object>> table = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        Iterator<Row> rowIterator = workbookSheet.rowIterator();
        int rowIdx = 0;
        boolean readData = false;
        dataRowIdx = Math.max(0, dataRowIdx);
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (rowIdx == headerRowIdx) {
                columns = _readHeader(row);
            }
            if (rowIdx == dataRowIdx && !readData) {
                readData = true;
            }
            if (readData) {
                Map<String, Object> rowMap = _readRowValues(row, columns, replaceMap);
                table.add(rowMap);
            }
            rowIdx++;
        }
        return table;
    }

    /**
     * Reads the entire section as a table.
     * 'Cascade' means that empty cells in the source workbook keeps the value across rows until modified
     * The section support ${...} tag replacement via optional object replaceMap
     *
     * @param headerRowIdx    - 0-based index of the header row; -1 means no header
     * @param dataRowIdx      - 0-based index of the first data row
     * @param replaceMap      - replaceMap [optional]
     * @param cascadeExcludes - array of columns to be excluded from the cascade [optional]
     * @return
     */
    public List<Map<String, Object>> readAsCascadeTable(int headerRowIdx, int dataRowIdx,
                                                        Map<String, String> replaceMap, String... cascadeExcludes) {

        List<Map<String, Object>> table = readAsTable(headerRowIdx, dataRowIdx, replaceMap);
        List<Map<String, Object>> cascadeTable = new ArrayList<>();
        Map<String, Object> prevRow = null;
        for (Map<String, Object> row : table) {
            //  remove blank or null fields
            //  to ensures that all blank fields to be correctly overwritten by cascaded values
            row = row.entrySet().stream()
                    .filter(e -> {
                        if (e.getValue() instanceof String) {
                            return !((String) e.getValue()).trim().equals("");
                        } else {
                            return true;
                        }
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));

            //  initialize the new row using values from the previous row
            Map<String, Object> cascadeRow = prevRow != null
                    ? new LinkedHashMap<>(prevRow)
                    : new LinkedHashMap<>();
            //  remove excluded columns
            if (cascadeExcludes != null) {
                Arrays.stream(cascadeExcludes).forEach(cascadeRow::remove);
            }
            //  merge the values read from the table into the new row
            cascadeRow.putAll(row);
            //  save row for the next cycle
            prevRow = cascadeRow;
            //  push into resulting table
            cascadeTable.add(cascadeRow);
        }
        return cascadeTable;
    }

    private Map<String, Object> _readRowValues(Row row, List<String> columns, Map<String, String> replaceMap) {
        FormulaEvaluator evaluator = workbookSheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        Iterator<Cell> cellIterator = row.cellIterator();
        Map<String, Object> rowResult = new LinkedHashMap<>();
        while (cellIterator.hasNext()) {
            Object value = null;
            Cell cell = cellIterator.next();
            switch (cell.getCellType()) {
                case CellType.NUMERIC:
                    value = cell.getNumericCellValue();
                    if (DateUtil.isCellDateFormatted(cell) && DateUtil.isValidExcelDate((Double) value)) {
                        value = cell.getLocalDateTimeCellValue();
                    }
                    break;
                case CellType.STRING:
                    value = cell.getStringCellValue();
                    if (value != null) {
                        value = _doReplace((String) value, replaceMap);
                        value = ReplaceHelper.removeNonPrintableChars(value.toString()).trim();
                    }
                    break;
                case CellType.FORMULA:
                    value = _evaluateAndRead(evaluator, cell);
                    break;
                case CellType.BLANK:
                    break;
                case CellType.BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case CellType.ERROR:
                    break;
                case CellType._NONE:
                default:
                    throw new IllegalStateException("Unexpected value: " + cell.getCellType());
            }

            String column = (columns != null && cell.getColumnIndex() < columns.size())
                    ? columns.get(cell.getColumnIndex())
                    : Integer.toString(cell.getColumnIndex());
            rowResult.put(column, value != null ? value : "");
        }

        return rowResult;
    }

    private Object _evaluateAndRead(FormulaEvaluator evaluator, Cell cell) {
        switch (evaluator.evaluateFormulaCell(cell)) {
            case CellType.NUMERIC:
                return cell.getNumericCellValue();
            case CellType.STRING:
                return cell.getStringCellValue();
            case CellType.BOOLEAN:
                return cell.getBooleanCellValue();
            case CellType.ERROR:
                return cell.getErrorCellValue();
            case CellType.BLANK:
                return "";
            case CellType._NONE:
            default:
                throw new IllegalStateException("Unexpected value: " + cell.getCellType());
        }
    }


    private List<String> _readHeader(Row row) {
        List<String> columns = new ArrayList<>();
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell.getCellType() == CellType.STRING) {
                columns.add(cell.getStringCellValue());
            } else {
                columns.add("");
            }
        }

        return columns;
    }


    private String _doReplace(String orig, Map<String, String> replaceMap) {
        String dest = orig;
        if (replaceMap != null) {
            for (String k : replaceMap.keySet()) {
                dest = dest.replace("${" + k.trim() + "}", replaceMap.get(k));
            }
        }
        return dest;
    }
}
