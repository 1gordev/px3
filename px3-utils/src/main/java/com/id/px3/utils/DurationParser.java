package com.id.px3.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {

    // Updated to include 'ms' for milliseconds
    private static final Pattern DURATION_PATTERN = Pattern.compile("(-?\\d+)([smhd]|ms)");

    public static @Nullable Duration parseNullable(String input) {
        if (input == null) {
            return null;
        }
        return parse(input);
    }

    /**
     * Parse human-readable duration string to Duration.
     *
     * @param input The input string (e.g., "10s", "-5m", "100ms").
     * @return Duration object.
     */
    public static @NotNull Duration parse(String input) {
        if (input.startsWith("PT")) {
            return Duration.parse(input);
        } else {
            Matcher matcher = DURATION_PATTERN.matcher(input);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid duration format: " + input);
            }

            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);

            return switch (unit) {
                case "s" -> Duration.ofSeconds(value);
                case "m" -> Duration.ofMinutes(value);
                case "h" -> Duration.ofHours(value);
                case "d" -> Duration.ofDays(value);
                case "ms" -> Duration.ofMillis(value);
                default -> throw new IllegalArgumentException("Unknown duration unit: " + unit);
            };
        }
    }
}
