package io.fluentcoding.codemanbot.bridge;

import io.fluentcoding.codemanbot.util.DateUtil;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.entries.ParticipantEntry;
import io.fluentcoding.codemanbot.util.entries.TournamentEntry;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChallongeBridge {
    private final static String URI = GlobalVar.dotenv.get("CHALLONGE_URI");

    public static TournamentEntry getTournament(String slug) {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {

            String baseUrl = URI + "/tournament/" + slug;
            HttpGet get;
            HttpResponse response;
            String responseContent;
            JSONArray entries;

            // GET THE PARTICIPANTS
            get = new HttpGet(baseUrl + "/participants.json");
            response = client.execute(get);

            responseContent = EntityUtils.toString(response.getEntity());
            entries = new JSONArray(responseContent);

            List<ParticipantEntry> participants = new ArrayList<>();

            if (entries.length() == 0)
                participants = null;
            else {
                for (int i = 0; i < entries.length(); i++) {
                    JSONObject participant = entries.getJSONObject(i).getJSONObject("participant");
                    participants.add(new ParticipantEntry(
                        StringUtil.stripDiscordMarkdown(participant.optString("display_name")),
                        null, // No connect code
                        participant.optInt("seed"),
                        participant.optInt("final_rank"),
                        null // No way to know if the ranks are final
                    ));
                }
            }

            // GET THE TOURNAMENT
            get = new HttpGet(baseUrl + ".json");
            response = client.execute(get);

            responseContent = EntityUtils.toString(response.getEntity());
            JSONObject tournament = new JSONObject(responseContent).getJSONObject("tournament");
            if (tournament.length() == 0)
                return null;
            else {
                return new TournamentEntry(
                    tournament.optString("name"),
                    tournament.optString("description"),
                    null, // No image profile
                    null, // No image banner
                    tournament.optString("game_name"),
                    tournament.optString("tournament_type"),
                    DateUtil.fromIsoTime(tournament.optString("start_at")),
                    tournament.optString("state"),
                    tournament.optInt("participants_count"),
                    null, // No events
                    participants
                );
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long ping() {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            long start = System.currentTimeMillis();

            HttpGet post = new HttpGet(URI);
            client.execute(post);

            return System.currentTimeMillis() - start;
        } catch(Exception e) {
            return -1;
        }
    }
}
