package io.fluentcoding.codemanbot.bridge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SlippiBridge {
    private final static String SLIPPI_GRAPHQL_URL = "https://slippi-hasura.herokuapp.com/v1/graphql";

    public static String getName(String code) {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(SLIPPI_GRAPHQL_URL);
            post.setEntity(new StringEntity("{\"operationName\":\"fetch\",\"variables\":{\"code\":\"" + code + "\"},\"query\":\"" +
                    "query fetch($code:String!){" +
                    "users(where:{connectCode:{_eq:$code}}){" +
                    "displayName status" +
                    "}" +
                    "}\"}"));
            HttpResponse response = client.execute(post);

            String json = EntityUtils.toString(response.getEntity());
            JSONObject object = new JSONObject(json).getJSONObject("data");
            JSONObject user = object.getJSONArray("users").getJSONObject(0);

            if (user.get("status").equals("active"))
                return user.getString("displayName");
            else
                return null;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean userWithCodeExists(String code) {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(SLIPPI_GRAPHQL_URL);

            JSONObject content = new JSONObject();
            content.put("operationName", "fetch");
            content.put("variables", new JSONObject().put("code", code));
            content.put("query", "query fetch($code:String!){users(where:{connectCode:{_eq:$code}}){status}}");

            post.setEntity(new StringEntity(content.toString()));
            HttpResponse response = client.execute(post);

            String json = EntityUtils.toString(response.getEntity());
            JSONObject object = new JSONObject(json);

            JSONArray users = object.getJSONObject("data").getJSONArray("users");

            return users.length() > 0;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    public static List<UserEntry> getCodesWithActualName(String name) {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(SLIPPI_GRAPHQL_URL);

            JSONObject content = new JSONObject();
            content.put("operationName", "fetch");
            content.put("variables", new JSONObject().put("name", name));
            content.put("query", "query fetch($name: String!){users(where:{displayName:{_ilike:$name}}){displayName connectCode status}}");

            post.setEntity(new StringEntity(content.toString()));
            HttpResponse response = client.execute(post);

            String json = EntityUtils.toString(response.getEntity());
            JSONObject object = new JSONObject(json);

            JSONArray users = object.getJSONObject("data").getJSONArray("users");
            if (users.length() == 0)
                return null;
            else {
                List<UserEntry> displayNames = new ArrayList<>();
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    if (!user.getString("status").equals("active"))
                        continue;

                    String connectCode = user.getString("connectCode");
                    if (!connectCode.equals("null")) {
                        String displayName = user.getString("displayName");
                        if (displayName.equals("null") || displayName.equals(name)) {
                            displayNames.add(new UserEntry(null, connectCode));
                        } else {
                            displayNames.add(new UserEntry(displayName, connectCode));
                        }
                    }
                }

                return displayNames;
            }
        } catch(Exception e) {
            return null;
        }
    }

    public static long ping() {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            long start = System.currentTimeMillis();

            HttpGet post = new HttpGet(SLIPPI_GRAPHQL_URL);
            client.execute(post);

            return System.currentTimeMillis() - start;
        } catch(Exception e) {
            return -1;
        }
    }

    @AllArgsConstructor
    @Getter
    public static class UserEntry {
        private String displayName;
        private String code;
    }
}
