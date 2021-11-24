package io.fluentcoding.codemanbot.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class ColorUtil {
    private static Map<String, Integer> colorMap = new HashMap<>();

    public static void init() throws JSONException {
        String jsonText = new BufferedReader(
                new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("colors.json"), StandardCharsets.UTF_8)
        ).lines().collect(Collectors.joining("\n"));

        JSONObject jsonObject = new JSONObject(jsonText);
        Iterator<String> keys = jsonObject.keys();

        while(keys.hasNext()) {
            String key = keys.next();
            String hexCode = jsonObject.getString(key);
            int color = Integer.parseInt(hexCode.substring(1), 16);

            colorMap.put(key, color);
        }
    }

    public static int getColorFromName(String name) {
        Integer result = colorMap.get(name);

        if (result != null)
            return result;
        else
            return -1;
    }
}
