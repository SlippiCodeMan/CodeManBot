package io.fluentcoding.codemanbot.bridge;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class SlippiRankBridge {
    private final static String SLPRANK_URL = "http://slprank.com/rank/";

    // TODO in a possible rewrite, we would probably want to rely on slprank for fetching the display name as well so we dont have
    // to make on extra commit
    public static Map<String, RankEntry> getRanks(String... codes) {
        Map<String, RankEntry> result = new HashMap<>();

        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(SLPRANK_URL + "?raw");
            post.setEntity(new StringEntity(new JSONArray(codes).toString(), ContentType.APPLICATION_JSON));

            HttpResponse response = client.execute(post);

            String json = EntityUtils.toString(response.getEntity());
            JSONObject map = new JSONObject(json);

            for (var it = map.keys(); it.hasNext();) {
                String key = (String) it.next();
                JSONObject user = map.getJSONObject(key);
                RankEntry rank = new RankEntry(user.getString("rank"), user.getInt("rating"), user.getInt("wins"), user.getInt("losses"));
                result.put(key, rank);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static RankEntry getRank(String code) {
        return getRanks(code).get(code);
    }

    public static long ping() {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            long start = System.currentTimeMillis();

            HttpGet get = new HttpGet(SLPRANK_URL);
            client.execute(get);

            return System.currentTimeMillis() - start;
        } catch(Exception e) {
            return -1;
        }
    }

    @AllArgsConstructor
    @Getter
    public static class RankEntry {
        private String rank;
        private int rating;
        private int wins;
        private int losses;

        public boolean hasPlayed() {
            return wins > 0 || losses > 0;
        }
    }
}
