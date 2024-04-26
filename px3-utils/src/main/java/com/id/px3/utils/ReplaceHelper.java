package com.id.px3.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceHelper {
    public static String caseInsensitiveReplace(String input, String target, String replacement, boolean replaceAll) {
        String escapedTarget = Pattern.quote(target);
        int flags = replaceAll ? Pattern.CASE_INSENSITIVE | Pattern.MULTILINE : Pattern.CASE_INSENSITIVE;
        Pattern pattern = Pattern.compile(escapedTarget, flags);
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();
        if (replaceAll) {
            while (matcher.find()) {
                matcher.appendReplacement(result, replacement);
            }
            matcher.appendTail(result);
        } else {
            boolean found = matcher.find();
            if (found) {
                matcher.appendReplacement(result, replacement);
            }
            matcher.appendTail(result);

            if (!found) {
                result = new StringBuilder(input);
            }
        }

        return result.toString();
    }

    public static String removeNonPrintableChars(String input) {
        return input.replaceAll("[^a-zA-Z0-9\\s!\"£$%&/()=?^*§°ç\\-.,;:_\\[\\]{}<>]", "");
    }
}
