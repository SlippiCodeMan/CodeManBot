package io.fluentcoding.codemanbot.bridge;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SlippiBridge {
    private final static String SLIPPI_GRAPHQL_URL = "https://slippi-hasura.herokuapp.com/v1/graphql";

    public static String getName(String code) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();

            HttpPost post = new HttpPost(SLIPPI_GRAPHQL_URL);
            post.setEntity(new StringEntity("{\"operationName\":\"fetch\",\"variables\":{\"code\":\"" + code + "\"},\"query\": \"fragment userDisplay on User {" +
                    "  displayName" +
                    "}" +
                    "query fetch($code: String!) {" +
                    "  users(where: { connectCode: { _eq: $code } }) {" +
                    "    ...userDisplay" +
                    "  }" +
                    "}\"}"));
            HttpResponse response = client.execute(post);

            String json = EntityUtils.toString(response.getEntity());
            JSONObject object = new JSONObject(json);

            return object.getJSONObject("data").getJSONArray("users").getJSONObject(0).getString("displayName");
        } catch(Exception e) {
            return null;
        }
    }

    public static List<String> getCodes(String name) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();

            HttpPost post = new HttpPost(SLIPPI_GRAPHQL_URL);
            post.setEntity(new StringEntity("{\"operationName\":\"fetch\",\"variables\":{\"name\":\"" + name + "\"},\"query\": \"fragment userDisplay on User {" +
                    "  displayName" +
                    "  connectCode" +
                    "}" +
                    "query fetch($name: String!) {" +
                    "  users(where: { displayName: { _ilike: $name } }) {" +
                    "    ...userDisplay" +
                    "  }" +
                    "}\"}"));
            HttpResponse response = client.execute(post);

            String json = EntityUtils.toString(response.getEntity());
            JSONObject object = new JSONObject(json);

            JSONArray users = object.getJSONObject("data").getJSONArray("users");
            if (users.length() == 0)
                return null;
            else {
                List<String> displayNames = new ArrayList<>();
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    String connectCode = user.getString("connectCode");
                    if (!connectCode.equals("null")) {
                        String displayName = user.getString("displayName");
                        if (displayName.equals("null") || displayName.equals(name)) {
                            displayNames.add(connectCode);
                        } else {
                            displayNames.add(connectCode + " ***(" + displayName + ")***");
                        }
                    }
                }

                return displayNames;
            }
        } catch(Exception e) {
            return null;
        }
    }

}
