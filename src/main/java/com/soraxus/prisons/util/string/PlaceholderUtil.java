package com.soraxus.prisons.util.string;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaceholderUtil {
    /**
     * Replace all instances of the placeholders in the string
     *
     * @param in           Input String
     * @param placeholders Placeholder Map
     * @return Replaced String
     */
    public static String replacePlaceholders(String in, Map<String, String> placeholders) {
        String out = in;
        for (Map.Entry<String, String> ent : placeholders.entrySet()) {
            out = out.replaceAll("\\{" + ent.getKey() + "}", ent.getValue());
        }
        return out;
    }

    /**
     * Replace all instances of the placeholders in every string
     *
     * @param in           List of strings
     * @param placeholders Placeholder Map
     * @return Replaced list of strings
     */
    public static List<String> replacePlaceholders(List<String> in, Map<String, String> placeholders) {
        return in.stream()
                .map(s -> replacePlaceholders(s, placeholders))
                .collect(Collectors.toList());
    }
}
