package io.fluentcoding.codemanbot.bridge;

import io.fluentcoding.codemanbot.util.DateUtil;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.entries.EventEntry;
import io.fluentcoding.codemanbot.util.entries.ParticipantEntry;
import io.fluentcoding.codemanbot.util.entries.TournamentEntry;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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
            content.put("query", "query fetch($slug:String!){tournament(slug:$slug){name,startAt,isOnline,images{url,type},events{name,state,startAt,standings(query:{page:1,perPage:8}){nodes{placement,isFinal,entrant{name,participants{connectedAccounts}seeds{seedNum}}}}}}");

            post.setEntity(new StringEntity(content.toString()));
            HttpResponse response = client.execute(post);

            String json = EntityUtils.toString(response.getEntity());
            JSONObject object = new JSONObject(json).getJSONObject("data");
            JSONObject tournamentObject = object.getJSONObject("tournament");
            JSONArray eventArray = tournamentObject.getJSONArray("events");

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
                            JSONObject entrant = participant.getJSONObject("entrant");
                            participants.add(new ParticipantEntry(
                                entrant.optString("name"),
                                entrant
                                        .getJSONArray("participants")
                                        .getJSONObject(0)
                                        .isNull("connectedAccounts") ? "" :
                                        entrant
                                                .getJSONArray("participants")
                                                .getJSONObject(0)
                                                .getJSONObject("connectedAccounts")
                                                .optJSONObject("slippi")
                                                .optString("value"),
                                entrant.isNull("seeds") ? null :
                                        entrant.getJSONArray("seeds").getJSONObject(0).getInt("seedNum"),
                                participant.optInt("placement"),
                                participant.optBoolean("isFinal")
                            ));
                        }
                        events.add(new EventEntry(
                                event.getString("name"),
                                event.getLong("startAt") * 1000,
                                EventEntry.EventState.safeValueOf(event.getString("state")),
                                participants));
                    }
                }

                JSONArray images = tournamentObject.getJSONArray("images");
                String imageProfile = "";
                String imageBanner = "";
                if (images.length() > 0) {
                    for (int i = 0; i < images.length(); i++) {
                        JSONObject image = images.getJSONObject(i);
                        String url = image.getString("url");
                        switch (image.getString("type")) {
                            case "profile":
                                imageProfile = url;
                                break;
                            case "banner":
                                imageBanner = url;
                                break;
                        }
                    }

                }

                return new TournamentEntry(
                    tournamentObject.getString("name"),
                    null, // Description, null atm
                    imageProfile,
                    imageBanner,
                    null,
                    null,
                    DateUtil.fromLong(tournamentObject.getLong("startAt")),
                    null,
                    null,
                    events,
                    null
                );
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
