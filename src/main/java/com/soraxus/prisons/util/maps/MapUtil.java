package com.soraxus.prisons.util.maps;

import net.ultragrav.serializer.GravSerializer;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {
    public static String mapToString(Map<String, String> map) {
        GravSerializer serializer = new GravSerializer();

        serializer.writeInt(map.size());
        map.forEach((k, v) -> {
            serializer.writeString(k);
            serializer.writeString(v);
        });

        return serializer.toString();
    }

    public static Map<String, String> stringToMap(String input) {
        Map<String, String> map = new HashMap<>();

        GravSerializer serializer = new GravSerializer(input);
        int a = serializer.readInt();
        for (int i = 0; i < a; i++) {
            map.put(serializer.readString(), serializer.readString());
        }

        return map;
    }
}