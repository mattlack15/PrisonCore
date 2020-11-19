package com.soraxus.prisons.crate.locker;

import com.soraxus.prisons.util.UltraCollectors;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class LockerUtil {
    public static String serialize(Map<String, Integer> in) {
        return in.entrySet().stream()
                .map(e -> e.getKey() + "##" + e.getValue())
                .collect(Collectors.joining("||"));
    }

    public static Map<String, Integer> deserialize(String in) {
        return Arrays.stream(in.split("\\|\\|"))
                .map(str -> new AbstractMap.SimpleEntry<>(
                        str.split("##")[0],
                        Integer.parseInt(str.split("##")[1])
                ))
                .collect(UltraCollectors.toMap());
    }

    public static int calculateCap(int tier) {
        return (int) Math.floor(50 * Math.pow(3, tier));
    }
}