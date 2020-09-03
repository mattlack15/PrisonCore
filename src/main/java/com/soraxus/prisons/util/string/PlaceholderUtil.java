package com.soraxus.prisons.util.string;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaceholderUtil {
    public static String replacePlaceholders(String in, Map<String, String> placeholders) {
        String out = in;
        for (Map.Entry<String, String> ent : placeholders.entrySet()) {
            out = out.replaceAll("\\{" + ent.getKey() + "}", ent.getValue());
        }
        return out;
    }

    public static List<String> replacePlaceholders(List<String> in, Map<String, String> placeholders) {
        return in.stream()
                .map(s -> replacePlaceholders(s, placeholders))
                .collect(Collectors.toList());
    }
}
