package com.id.px3.utils;

import java.text.NumberFormat;
import java.util.*;

public class SafeConvert {

    /**
     * Convert an object to a string.
     * Return an optional string.
     *
     * @param obj source object
     * @return optional string
     */
    public static Optional<String> toString(Object obj) {
        String ret = null;
        try {
            ret = (obj instanceof String)
                    ? (String) obj
                    : (obj != null ? obj.toString() : null);
        } catch (Exception ignored) {
        }
        return Optional.ofNullable(ret);
    }

    /**
     * Convert an object to a string list.
     * The expected object should be a csv string using the given separator.
     * Each element of the returned list is trimmed.
     *
     * @param obj       source object
     * @param separator separator
     * @return optional string list
     */
    public static Optional<List<String>> toStringList(Object obj, String separator) {
        List<String> ret = null;
        try {
            var str = toString(obj).orElse(null);
            ret = str != null
                    ? Arrays.stream(str.split(separator)).map(String::trim).toList()
                    : null;
        } catch (Exception ignored) {
        }
        return Optional.ofNullable(ret);
    }

    public static Optional<List<String>> toStringList(Object obj) {
        return toStringList(obj, ";");
    }

    /**
     * Convert an object to a string map.
     *
     * @param obj         source object
     * @param rowSeparator row separator
     * @param equals      key-value separator
     * @return optional string map
     */
    public static Optional<Map<String, String>> toStringMap(Object obj, String rowSeparator, String equals) {
        Map<String, String> ret = null;
        try {
            ret = new HashMap<>();
            Map<String, String> finalRet = ret;
            toStringList(obj, rowSeparator).ifPresent(list -> {
                list.forEach(row -> {
                    var kv = row.split(equals);
                    if (kv.length == 2) {
                        finalRet.put(kv[0].trim(), kv[1].trim());
                    }
                });
            });
        } catch (Exception ignored) {
        }
        return Optional.ofNullable(ret);
    }

    public static Optional<Map<String, String>> toStringMap(Object obj) {
        return toStringMap(obj, ";", "=");
    }


    /**
     * Convert the content of the given Object to a Double.
     *
     * @param obj source object
     * @return optional Double - can be empty
     */
    public static Optional<Double> toDouble(Object obj) {
        Double ret = null;
        try {
            if (obj instanceof Double) {
                return Optional.of((Double) obj);
            } else {
                //  convert all commas to point
                String str = obj.toString()
                        .trim()
                        .replaceAll(",", ".")
                        //  keep only the last point
                        .replaceAll("\\.(?=.*\\.)", "");

                //  convert to double
                ret = NumberFormat.getInstance(Locale.ROOT).parse(str).doubleValue();
            }
        } catch (Exception ignored) {
        }
        return Optional.ofNullable(ret);
    }

    /**
     * Convert the content of the given Object to a Long.
     *
     * @param obj source object
     * @return optional Long - can be empty
     */
    public static Optional<Long> toLong(Object obj) {
        Long ret = null;
        try {
            if (obj instanceof Long) {
                return Optional.of((Long) obj);
            } else {
                //  convert to double
                ret = NumberFormat.getInstance(Locale.ROOT).parse(obj.toString().trim()).longValue();
            }
        } catch (Exception ignored) {
        }
        return Optional.ofNullable(ret);
    }

    /**
     * Convert the content of the given Object to a Boolean.
     *
     * @param obj
     * @return
     */
    public static Optional<Boolean> toBoolean(Object obj) {
        try {
            if (obj instanceof Boolean) {
                return Optional.of((Boolean) obj);
            } else if (obj instanceof Integer) {
                return Optional.of(((Integer) obj) == 1);
            } else if (obj instanceof Long) {
                return Optional.of(((Long) obj) == 1);
            } else if (obj instanceof Double) {
                return Optional.of(((Double) obj).longValue() == 1);
            } else {
                List<String> trues = List.of("1", "true", "yes", "y", "checked", "on", "t");
                if (trues.contains(toString(obj).orElse("").trim().toLowerCase())) {
                    return Optional.of(true);
                }
            }
        } catch (Exception ignored) {
        }
        return Optional.of(false);
    }
}
