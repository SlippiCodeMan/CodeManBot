package io.fluentcoding.codemanbot.bridge;

import io.fluentcoding.codemanbot.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import io.fluentcoding.codemanbot.util.GlobalVar;

import java.util.ArrayList;
import java.util.List;

public class ChallongeBridge {
    private final static String URI = GlobalVar.dotenv.get("CHALLONGE_URI");

    public static List<ParticipantEntry> getParticipants(String tournamentName) {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet get = new HttpGet(URI
                    + "/tournaments/"
                    + tournamentName
                    + "/participants.json");
            HttpResponse response = client.execute(get);

            String json = EntityUtils.toString(response.getEntity());
            JSONArray entries = new JSONArray(json);
            if (entries.length() == 0)
                return null;
            else {
                List<ParticipantEntry> participants = new ArrayList<>();
                for (int i = 0; i < entries.length(); i++) {
                    JSONObject participant = entries.getJSONObject(i).getJSONObject("participant");
                    participants.add(new ParticipantEntry(
                        StringUtil.stripDiscordMarkdown(participant.getString("display_name")),
                        participant.getBoolean("checked_in"),
                        participant.getInt("seed"),
                        participant.isNull("final_rank") ? 0 : participant.getInt("final_rank")
                    ));
                }
                return participants;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static TournamentEntry getTournament(String tournamentName) {
        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet get = new HttpGet(URI
                    + "/tournaments/"
                    + tournamentName
                    + ".json");
            HttpResponse response = client.execute(get);

            String json = EntityUtils.toString(response.getEntity());
            JSONObject tournament = new JSONObject(json).getJSONObject("tournament");
            if (tournament.length() == 0)
                return null;
            else {
                return new TournamentEntry(
                    tournament.getString("name"),
                    tournament.getString("description"),
                    tournament.getString("game_name"),
                    tournament.getString("tournament_type"),
                    tournament.getString("start_at"),
                    tournament.getString("state"),
                    tournament.getInt("participants_count")
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

    @AllArgsConstructor
    @Getter
    public static class ParticipantEntry {
        private String displayName;
        private boolean checkedIn;
        private int seed;
        private int finalRank;
    }

    @AllArgsConstructor
    @Getter
    public static class TournamentEntry {
        private String name;
        private String description;
        private String gameName;
        private String type;
        private String startsAt;
        private String state;
        private int participantsCount;
    }
}