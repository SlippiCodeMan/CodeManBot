package io.fluentcoding.codemanbot.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ColorUtil {
    private static Map<String, Integer> colorMap = new HashMap<>();

    public static void init() throws URISyntaxException, IOException, JSONException {
        URL resource = ColorUtil.class.getClassLoader().getResource("colors.json");
        byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
        String jsonText = new String(bytes);

        JSONObject jsonObject = new JSONObject(jsonText);
        Iterator<String> keys = jsonObject.keys();

        while(keys.hasNext()) {
            String key = keys.next();
            String hexCode = jsonObject.getString(key);
            int color = Integer.parseInt(hexCode, 16);

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
