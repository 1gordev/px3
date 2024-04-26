package com.id.px3.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class MapExtractor {
    /**
     * Fetches the value from a nested map based on a dot-separated path.
     *
     * @param map  The map to fetch the value from.
     * @param path The path, e.g. "var1.var2.var3".
     * @return The value, or null if not found.
     */
    public static Optional<Object> fromPath(Map<String, Object> map, String path) {
        if (path != null && !path.isBlank()) {
            //  split path parts
            String[] pathParts = path.split("\\.", 2);
            if (map.containsKey(pathParts[0])) {
                if (pathParts.length > 1) {
                    if (map.get(pathParts[0]) instanceof Map<?, ?>) {
                        //  go deeper inside the map
                        //noinspection unchecked
                        return fromPath((Map<String, Object>) map.get(pathParts[0]), pathParts[1]);
                    } else {
                        //  next part of the path found, but it's not a map
                        log.error("Left part '%s' of partial path '%s' is not a map".formatted(pathParts[0], path));
                        return Optional.empty();
                    }
                } else {
                    //  may be null
                    return Optional.ofNullable(map.get(pathParts[0]));
                }
            }
        }
        return Optional.empty();
    }

}
