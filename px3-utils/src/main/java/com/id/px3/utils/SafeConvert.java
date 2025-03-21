package com.id.px3.utils;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;

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
     * Converts an object to a list of strings split by rowSeparator (Pattern).
     *
     * @param obj           source object
     * @param rowSeparator  row separator as a Pattern
     * @return optional list of strings
     */
    public static Optional<List<String>> toStringList(Object obj, Pattern rowSeparator) {
        if (obj == null) {
            return Optional.empty();
        }
        String str = obj.toString();
        String[] rows = rowSeparator.split(str); // Split using Pattern
        return Optional.of(Arrays.asList(rows));
    }

    /**
     * Converts an object to a list of strings with a single-character row separator.
     *
     * @param obj           source object
     * @param rowSeparator  single character row separator
     * @param trim          trim each string
     * @return optional list of strings
     */
    public static Optional<List<String>> toStringList(Object obj, String rowSeparator, boolean trim) {
        return toStringList(obj, rowSeparator).map(lst -> trim ? lst.stream().map(String::trim).toList() : lst);
    }

    public static Optional<List<String>> toStringList(Object obj, String rowSeparator) {
        // Use default separator `;` if no rowSeparator is specified
        Pattern separatorPattern = rowSeparator == null || rowSeparator.length() != 1
                ? Pattern.compile(";")
                : Pattern.compile(Pattern.quote(rowSeparator));
        return toStringList(obj, separatorPattern);
    }

    public static Optional<List<String>> toStringList(Object obj) {
        return toStringList(obj, ";");
    }

    /**
     * Convert an object to a string map with a custom row separator.
     *
     * @param obj          source object
     * @param rowSeparator row separator as a Pattern
     * @param equals       key-value separator
     * @return optional string map
     */
    public static Optional<Map<String, String>> toStringMap(Object obj, Pattern rowSeparator, String equals) {
        Map<String, String> ret = null;
        try {
            ret = new LinkedHashMap<>();
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

    /**
     * Convert an object to a string map with a single-character row separator.
     *
     * @param obj          source object
     * @param rowSeparator single character row separator
     * @param equals       key-value separator
     * @return optional string map
     */
    public static Optional<Map<String, String>> toStringMap(Object obj, String rowSeparator, String equals, boolean trim) {
        return toStringMap(obj, rowSeparator, equals).map(map -> {
            if (trim) {
                return map.entrySet().stream()
                        .collect(LinkedHashMap::new,
                                (m, e) -> m.put(e.getKey().trim(), e.getValue().trim()),
                                LinkedHashMap::putAll);
            } else {
                return map;
            }
        });
    }

    public static Optional<Map<String, String>> toStringMap(Object obj, String rowSeparator, String equals) {
        // Use default separator `;` if no rowSeparator is specified
        Pattern separatorPattern = rowSeparator == null || rowSeparator.length() != 1
                ? Pattern.compile(";")
                : Pattern.compile(Pattern.quote(rowSeparator));
        return toStringMap(obj, separatorPattern, equals);
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
                List<String> trues = List.of("1.0", "1", "true", "yes", "y", "checked", "on", "t");
                if (trues.contains(toString(obj).orElse("").trim().toLowerCase())) {
                    return Optional.of(true);
                }
            }
        } catch (Exception ignored) {
        }
        return Optional.of(false);
    }

    /**
     * Keep only alphanumeric characters from the given string.
     *
     * @param str - source string
     * @param allowSpaces - allow spaces in the result
     *
     * @return true if the string satisfies the condition
     */
    public static boolean isAlphaNumeric(String str, boolean allowSpaces) {
        // use regex to check if the string is alphanumeric
        if (str != null) {
            if (allowSpaces) {
                return str.matches("^[a-zA-Z0-9 ]*$");
            } else {
                return str.matches("^[a-zA-Z0-9]*$");
            }
        }
        return false;
    }

}
