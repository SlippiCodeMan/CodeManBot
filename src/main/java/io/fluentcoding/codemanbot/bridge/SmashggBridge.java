package io.fluentcoding.codemanbot.bridge;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import io.fluentcoding.codemanbot.util.GlobalVar;

import java.util.ArrayList;
import java.util.List;

public class SmashggBridge {
    private final static String SMASHGG_GRAPHQL_URL = "https://api.smash.gg/gql/alpha";
    private final static String SMASHGG_AUTH = GlobalVar.dotenv.get("SMASHGG_AUTH");

    public static TournamentEntry getTournament(String slug) {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(SMASHGG_GRAPHQL_URL);
            post.addHeader("Authorization", "Bearer " + SMASHGG_AUTH);
            post.addHeader("Content-Type", "application/json");

            JSONObject content = new JSONObject();
            content.put("operationName", "fetch");
            content.put("variables", new JSONObject().put("slug", slug));
            content.put("query", "query fetch($slug:String!){tournament(slug:$slug){name,startAt,isOnline,images{url},events{name,standings(query:{page:1,perPage:9}){nodes{placement,isFinal,entrant{name,participants{connectedAccounts}seeds{seedNum}}}}}owner{name,slug,images{url}}}}");

            post.setEntity(new StringEntity(content.toString()));
            HttpResponse response = client.execute(post);

            String json = EntityUtils.toString(response.getEntity());
            JSONObject object = new JSONObject(json).getJSONObject("data");
            JSONObject tournamentObject = object.getJSONObject("tournament");
            JSONObject ownerObject = tournamentObject.getJSONObject("owner");
            JSONArray eventArray = tournamentObject.getJSONArray("events");

            OwnerEntry owner = new OwnerEntry(
                ownerObject.getString("name"),
                ownerObject.getString("slug"),
                ownerObject.getJSONArray("images").optJSONObject(0).optString("url"));

            if (eventArray.length() == 0)
                return null;
            else {
                List<EventEntry> events = new ArrayList<>();
                for (int i = 0; i < eventArray.length(); i++) {
                    JSONObject event = eventArray.getJSONObject(i);
                    JSONArray participantArray = event.getJSONObject("standings").getJSONArray("nodes");
                    if (participantArray.length() == 0)
                        return null;
                    else {
                        List<ParticipantEntry> participants = new ArrayList<>();
                        for (int j = 0; j < participantArray.length(); j++) {
                            JSONObject participant = participantArray.getJSONObject(j);
                            participants.add(new ParticipantEntry(
                                participant.getJSONObject("entrant").optString("name"),
                                participant.getJSONObject("entrant")
                                        .getJSONArray("participants")
                                        .getJSONObject(0)
                                        .getJSONObject("connectedAccounts")
                                        .optJSONObject("slippi")
                                        .optString("value"),
                                participant.getJSONObject("entrant").getJSONArray("seeds").getJSONObject(0).optInt("seedNum"),
                                participant.optInt("placement"),
                                participant.optBoolean("isFinal")
                            ));
                        }
                        events.add(new EventEntry(event.getString("name"), participants));
                    }
                }

                JSONArray images = tournamentObject.getJSONArray("images");
                String imageProfile = "";
                String imageBanner = "";
                if (images.length() > 0) {
                    for (int i = 0; i < images.length(); i++) {
                        JSONObject image = images.getJSONObject(i);
                        switch (image.getString("type")) {
                            case "profile":
                                imageProfile = image.getString("url");
                                break;
                            case "banner":
                                imageBanner = image.getString("url");
                                break;
                        }
                    }

                }

                return new TournamentEntry(
                    tournamentObject.optString("name"),
                    imageProfile,
                    imageBanner,
                    tournamentObject.optLong("startAt"),
                    tournamentObject.optBoolean("isOnline"),
                    owner,
                    events
                );
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @AllArgsConstructor
    @Getter
    public static class ParticipantEntry {
        private String name;
        private String connectCode;
        private int seed;
        private int placement;
        private boolean isPlacementFinal;
    }
    @AllArgsConstructor
    @Getter
    public static class OwnerEntry {
        private String name;
        private String slug;
        private String image;
    }
    @AllArgsConstructor
    @Getter
    public static class EventEntry {
        private String name;
        private List<ParticipantEntry> standings;
    }
    @AllArgsConstructor
    @Getter
    public static class TournamentEntry {
        private String name;
        private String imageProfile;
        private String imageBanner;
        private long startsAt;
        private boolean isOnline;
        private OwnerEntry owner;
        private List<EventEntry> events;
    }
}
