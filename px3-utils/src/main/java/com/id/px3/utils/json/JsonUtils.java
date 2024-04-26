package com.id.px3.utils.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {
    private static final Pattern ARRAY_PATTERN = Pattern.compile("(.+)\\[(\\d+)]");

    /**
     * Convert a bean to a nested map of objects
     *
     * @param src the bean to convert
     * @return the nested map
     */
    public static Map<String, Object> beanToNestedMap(Object src) {
        if (src != null) {
            Gson gson = newGson();
            String json = gson.toJson(src);
            return gson.fromJson(json, new TypeToken<Map<String, Object>>() {
            }.getType());
        }
        return null;
    }

    /**
     * Convert a bean to a nested json with support for Instant and Duration
     *
     * @return
     */
    public static Gson newGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }

    /**
     * Convert a flat map to a nested map of objects. Uses the dot as a separator.
     *
     * @param flatMap the flat map to convert
     * @return the nested map
     */
    public static Map<String, Object> flatMapToNestedMap(Map<String, Object> flatMap) {
        Map<String, Object> nestedMap = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String[] keys = entry.getKey().split("\\.");
            Map<String, Object> currentMap = nestedMap;

            for (int i = 0; i < keys.length; i++) {
                Matcher matcher = ARRAY_PATTERN.matcher(keys[i]);
                String key = keys[i];
                boolean isArrayElement = matcher.matches();

                if (isArrayElement) {
                    key = matcher.group(1); // get the key without the index
                }

                if (i < keys.length - 1 || isArrayElement) {
                    Object nestedValue = currentMap.get(key);
                    if (isArrayElement) {
                        if (!(nestedValue instanceof List)) {
                            nestedValue = new ArrayList<>();
                            currentMap.put(key, nestedValue);
                        }
                        //noinspection unchecked
                        List<Object> list = (List<Object>) nestedValue;
                        if (matcher.matches()) {
                            //  add null placeholders if necessary to handle non-sequential indices
                            int index = Integer.parseInt(matcher.group(2));
                            while (list.size() <= index) {
                                list.add(null);
                            }
                            //  prepare the map for the next key or set the value if at the end
                            if (i == keys.length - 1) {
                                list.set(index, entry.getValue());
                            } else {
                                if (list.get(index) == null || !(list.get(index) instanceof Map)) {
                                    list.set(index, new LinkedHashMap<>());
                                }
                                //noinspection unchecked
                                currentMap = (Map<String, Object>) list.get(index);
                            }
                        }
                    } else {
                        if (!(nestedValue instanceof Map)) {
                            nestedValue = new LinkedHashMap<>();
                            currentMap.put(key, nestedValue);
                        }
                        //noinspection unchecked
                        currentMap = (Map<String, Object>) nestedValue;
                    }
                } else {
                    //  leaf
                    currentMap.put(key, entry.getValue());
                }
            }
        }

        cleanNullsInLists(nestedMap);
        return nestedMap;
    }

    /**
     * Convert a flat map to a nested json. Uses the dot as a separator.
     *
     * @param flatMap the flat map to convert
     * @return the nested json
     */
    public static String flatMapToNestedJson(Map<String, Object> flatMap) {
        return newGson().toJson(flatMapToNestedMap(flatMap));
    }

    /**
     * Traverses a nested map using a JSON path as key.
     *
     * @param nestedMap the nested map to traverse
     * @param jsonPath  the JSON path as key
     * @return the object found, or null if not found
     */
    public static Object extractValueByJsonPath(Map<String, Object> nestedMap, String jsonPath) {
        //  split the path by dot, but not if it's inside square brackets
        String[] keys = jsonPath.split("\\.(?![^\\[]*])");
        Object currentValue = nestedMap;

        for (String key : keys) {
            //  check if we are accessing an index of a list
            if (key.matches(".+\\[\\d+]$")) {
                int index = Integer.parseInt(key.replaceAll(".*\\[(\\d+)]$", "$1"));
                key = key.replaceAll("\\[\\d+]", "");

                if (currentValue instanceof Map<?, ?>) {
                    currentValue = ((Map<?, ?>) currentValue).get(key);
                } else {
                    //  not a map, so the path is invalid at this point
                    return null;
                }

                if (currentValue instanceof List<?>) {
                    List<?> list = (List<?>) currentValue;
                    if (index < list.size()) {
                        currentValue = list.get(index);
                    } else {
                        //  index out of bounds
                        return null;
                    }
                } else {
                    //  not a list, so the path is invalid when expecting an index
                    return null;
                }
            } else {
                if (currentValue instanceof Map<?, ?>) {
                    currentValue = ((Map<?, ?>) currentValue).get(key);
                } else {
                    //  not a map, so the path is invalid or the key is not found
                    return null;
                }
            }

            if (currentValue == null) {
                //  if we encounter a null, the object does not exist at this path
                return null;
            }
        }

        return currentValue;
    }

    private static void cleanNullsInLists(Object object) {
        if (object instanceof Map<?, ?> map) {
            for (Object key : map.keySet()) {
                cleanNullsInLists(map.get(key));
            }
        } else if (object instanceof List<?> list) {
            list.removeIf(Objects::isNull);

            //  after removing nulls, recursively clean the remaining objects
            for (Object item : list) {
                cleanNullsInLists(item);
            }
        }
    }
}