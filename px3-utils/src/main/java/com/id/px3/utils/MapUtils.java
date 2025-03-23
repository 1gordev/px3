package com.id.px3.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;

public class MapUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static LinkedHashMap<String, String> toMap(Object obj) {
        if (obj == null) {
            return new LinkedHashMap<>();
        } else {
            LinkedHashMap<String, String> result = new LinkedHashMap<>();
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String key = field.getName();
                Object value = field.get(obj);
                if (value == null) {
                    result.put(key, null);
                } else if (isSimple(value)) {
                    if (value instanceof Number) {
                        if (value instanceof Integer || value instanceof Long) {
                            result.put(key, String.format(Locale.ROOT, "%d", value));
                        } else if (value instanceof Float || value instanceof Double) {
                            result.put(key, String.format(Locale.ROOT, "%f", value));
                        } else if (value instanceof BigDecimal) {
                            result.put(key, ((BigDecimal) value).toPlainString());
                        } else {
                            result.put(key, value.toString());
                        }
                    } else {
                        result.put(key, value.toString());
                    }
                } else {
                    String json = objectMapper.writeValueAsString(value);
                    result.put(key, json);
                }
            }
            return result;
        }
    }

    private static boolean isSimple(Object value) {
        return value instanceof String ||
               value instanceof Number ||
               value instanceof Boolean ||
               value instanceof Character;
    }
}
