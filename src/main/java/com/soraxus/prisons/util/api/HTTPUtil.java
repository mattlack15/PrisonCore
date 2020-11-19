package com.soraxus.prisons.util.api;

import net.ultragrav.serializer.Meta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HTTPUtil {
    public static List<String> getReq(String str) {
        try {
            URL url = new URL(str);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.getResponseCode();
            List<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            conn.disconnect();
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Meta getReqJson(String url) {
        List<String> res = getReq(url);
        if (res == null) {
            return null;
        }
        String str = String.join(" ", res);
        return Meta.fromJson(str);
    }
}
