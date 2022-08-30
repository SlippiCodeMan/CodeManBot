package io.fluentcoding.codemanbot.bridge;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import lombok.Data;
import org.bson.Document;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseBridge {
    private final static String mongoUri = GlobalVar.dotenv.get("CODEMAN_DB_URI");

    public static ToggleMainResult toggleMain(long discordId, SSBMCharacter main) {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            ToggleMainResult result;

            List<SSBMCharacter> oldMains = getMains(discordId);
            List<SSBMCharacter> newMains;

            if (oldMains == null) {
                newMains = new ArrayList<>();
                newMains.add(main);
                result = ToggleMainResult.acceptedAndFirstCreation(newMains);
            } else {
                newMains = new ArrayList<>(oldMains);
                if (newMains.contains(main)) {
                    newMains.remove(main);
                    result = ToggleMainResult.acceptedAndRemoved(oldMains, newMains);
                } else {
                    if (newMains.size() >= 3)
                        return ToggleMainResult.declinedAndListFull(oldMains);
                    newMains.add(main);
                    result = ToggleMainResult.acceptedAndAdded(oldMains, newMains);
                }
            }

            BasicDBObject filter = new BasicDBObject("discord_id", discordId);
            codeManCollection.updateOne(filter, Updates.set("mains", newMains.stream().map(ssbmCharacter -> ssbmCharacter.ordinal()).collect(Collectors.toList())));
            return result;
        }
    }

    public static long usersWithMains() {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            return codeManCollection.countDocuments(Filters.exists("mains"));
        }
    }

    public static long usersWithColors() {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            return codeManCollection.countDocuments(Filters.exists("color"));
        }
    }

    @Nullable
    public static List<SSBMCharacter> getMains(long discordId) {
        if (discordId == -1)
            return null;

        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            for (Document result : codeManCollection.find(new BasicDBObject("discord_id", discordId))) {
                if (result.containsKey("mains")) {
                    return result.getList("mains", Integer.class).stream()
                            .filter(main -> main < SSBMCharacter.values().length)
                            .map(main -> SSBMCharacter.values()[main]).collect(Collectors.toList());
                } else {
                    return null;
                }
            }

            return null;
        }
    }

    public static boolean insertColor(long discordId, int color) {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            BasicDBObject filter = new BasicDBObject("discord_id", discordId);
            FindIterable<Document> result = codeManCollection.find(filter);
            if (result.cursor().hasNext()) {
                codeManCollection.updateOne(filter, Updates.set("color", color));
                return true;
            }

            return false;
        }
    }

    public static int getColor(long discordId) {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            for (Document result : codeManCollection.find(new BasicDBObject("discord_id", discordId))) {
                if (result.containsKey("color")) {
                    return result.getInteger("color"); // Converts the hex color into a color object
                } else {
                    return GlobalVar.SUCCESS.getRGB(); // default color
                }
            }

            return GlobalVar.SUCCESS.getRGB();
        }
    }

    public static void cleanPreviousUser(String code) {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);
            codeManCollection.deleteOne(new BasicDBObject("slippi_code", code));
        }
    }

    public static InsertCodeResult insertCode(long discordId, String code) {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            if (codeManCollection.countDocuments(new BasicDBObject("slippi_code", code)) > 0) {
                return InsertCodeResult.declined();
            }

            BasicDBObject filter = new BasicDBObject("discord_id", discordId);
            FindIterable<Document> result = codeManCollection.find(filter);
            if (result.cursor().hasNext()) {
                String oldCode = result.cursor().next().getString("slippi_code");
                codeManCollection.updateOne(filter, Updates.set("slippi_code", code));
                return InsertCodeResult.accepted(oldCode);
            } else {
                codeManCollection.insertOne(new Document("discord_id", discordId).append("slippi_code", code));

                return InsertCodeResult.acceptedAndFirstCreation();
            }
        }
    }

    @Nullable
    public static String getCode(long discordId) {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            for (Document result : codeManCollection.find(new BasicDBObject("discord_id", discordId))) {
                if (result.containsKey("slippi_code")) {
                    return (String) result.get("slippi_code");
                } else {
                    return null;
                }
            }

            return null;
        }
    }

    public static long getDiscordIdFromConnectCode(String connectCode) {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            for (Document result : codeManCollection.find(new BasicDBObject("slippi_code", connectCode))) {
                if (result.containsKey("discord_id")) {
                    return (long) result.get("discord_id");
                } else {
                    return -1L;
                }
            }

            return -1L;
        }
    }

    public static void removeData(long discordId) {
        try (MongoClient client = getClient()) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            codeManCollection.deleteOne(new BasicDBObject("discord_id", discordId));
        }
    }

    public static long countDatabase() {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            return getCollection(client).countDocuments();
        }
    }

    private static MongoCollection<Document> getCollection(MongoClient client) {
        return client.getDatabase("codeman").getCollection("player_entries");
    }

    private static MongoClient getClient() {
        return MongoClients.create(mongoUri);
    }

    @Data
    public static class InsertCodeResult {
        private final String oldCode;
        private final boolean isAccepted;
        private final boolean firstCreation;

        public static InsertCodeResult declined() {
            return new InsertCodeResult(null, false, false);
        }
        public static InsertCodeResult accepted(String oldCode) {
            return new InsertCodeResult(oldCode, true, false);
        }
        public static InsertCodeResult acceptedAndFirstCreation() {
            return new InsertCodeResult(null, true, true);
        }
    }

    @Data
    public static class ToggleMainResult {
        private final List<SSBMCharacter> oldMains;
        private final List<SSBMCharacter> newMains;
        private final boolean isAccepted;
        private final boolean isAdding;

        public static ToggleMainResult declinedAndListFull(List<SSBMCharacter> oldMains) {
            return new ToggleMainResult(oldMains, null, false, false);
        }
        public static ToggleMainResult acceptedAndAdded(List<SSBMCharacter> oldMains, List<SSBMCharacter> newMains) {
            return new ToggleMainResult(oldMains, newMains, true, true);
        }
        public static ToggleMainResult acceptedAndRemoved(List<SSBMCharacter> oldMains, List<SSBMCharacter> newMains) {
            return new ToggleMainResult(oldMains, newMains, true, false);
        }
        public static ToggleMainResult acceptedAndFirstCreation(List<SSBMCharacter> newMains) {
            return new ToggleMainResult(null, newMains, true, true);
        }
    }
}