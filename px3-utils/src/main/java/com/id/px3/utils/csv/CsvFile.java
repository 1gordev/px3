package com.id.px3.utils.csv;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class CsvFile {


    private String csvText = "";

    public CsvFile() {
    }

    private CsvFile(String csvText) {
        this.csvText = csvText;
    }

    @SneakyThrows
    public static CsvFile fromStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return new CsvFile(reader.lines().collect(Collectors.joining("\n")));
        }
    }

    @SneakyThrows
    public List<Map<String, Object>> readAsTable() {
        return readAsTable(',');
    }

    @SneakyThrows
    public List<Map<String, Object>> readAsTable(char separator) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        //  use try-with-resources to auto-close the reader
        try (StringReader sr = new StringReader(csvText);
             BufferedReader br = new BufferedReader(sr)) {

            //  read headers
            String[] headers = Arrays.stream(br.readLine().split(Character.toString(separator)))
                    .map(String::trim)
                    .toArray(String[]::new);

            //  read rows
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = Arrays.stream(line.split(Character.toString(separator)))
                        .map(String::trim)
                        .toArray(String[]::new);
                if (values.length > 0) {
                    Map<String, Object> row = IntStream.range(0, Math.min(values.length, headers.length))
                            .boxed()
                            .collect(Collectors.toMap(i -> headers[i], i -> values[i], (a, b) -> b, LinkedHashMap::new));
                    resultList.add(row);
                } else {
                    log.warn("Invalid line (%s) in CSV file".formatted(line));
                }

            }
        }

        return resultList;
    }

    /**
     * Escape a CSV field.
     * Quote the field string with double-quotes if:
     * <ul>
     *     <li>field contains a comma</li>
     *     <li>field contains spaces</li>
     *     <li>field contains a double-quote (replaced with 2 double-quotes)</li>
     * </ul>
     *
     * @param field the field to escape.
     * @return the escaped field.
     */
    public static String escapeCsvField(String field) {
        if (field == null)
            return "";
        String quote = "";
        StringBuilder out = new StringBuilder();
        for (int j = 0; j < field.length(); j++) {
            char ch = field.charAt(j);
            if (ch == '\"') {
                quote = "\"";
                out.append("\"\"");
            } else {
                out.append(ch);
                if (ch == ',' || (Character.isSpaceChar(ch) && ch != ' '))
                    quote = "\"";
            }
        }
        return quote + out + quote;
    }
}
