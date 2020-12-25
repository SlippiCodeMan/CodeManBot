package io.fluentcoding.codemanbot.bridge;

import io.fluentcoding.codemanbot.util.GlobalVar;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import lombok.Data;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseBridge {
    private final static String mongoUri = GlobalVar.dotenv.get("CODEMAN_DB_URI");

    public static ToggleMainResult toggleMain(long discordId, SSBMCharacter main) {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            List<SSBMCharacter> mains = getMains(discordId);

            if (mains == null) {
                mains = new ArrayList<>();
                mains.add(main);
            } else {
                if (mains.contains(main)) {
                    mains.remove(main);
                } else {
                    if (mains.size() >= 3)
                        return ToggleMainResult.listFull(mains);
                    mains.add(main);
                }
            }

            BasicDBObject filter = new BasicDBObject("discord_id", discordId);
            codeManCollection.updateOne(filter, Updates.set("mains", mains.stream().map(ssbmCharacter -> ssbmCharacter.ordinal()).collect(Collectors.toList())));
            return ToggleMainResult.accepted(mains);
        }
    }

    public static List<SSBMCharacter> getMains(long discordId) {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            for (Document result : codeManCollection.find(new BasicDBObject("discord_id", discordId))) {
                if (result.containsKey("mains")) {
                    return result.getList("mains", Integer.class).stream()
                            .map(main -> SSBMCharacter.values()[main]).collect(Collectors.toList());
                } else {
                    return null;
                }
            }

            return null;
        }
    }

    public static InsertCodeResult insertCode(long discordId, String code) {
        try (MongoClient client = MongoClients.create(mongoUri)) {
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

    public static String getCode(long discordId) {
        try (MongoClient client = MongoClients.create(mongoUri)) {
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
        try (MongoClient client = MongoClients.create(mongoUri)) {
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
        try (MongoClient client = MongoClients.create(mongoUri)) {
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
        return client.getDatabase("player_codes").getCollection("codeman");
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
        private final List<SSBMCharacter> mains;
        private final boolean isAccepted;

        public static ToggleMainResult listFull(List<SSBMCharacter> oldMains) {
            return new ToggleMainResult(oldMains, false);
        }
        public static ToggleMainResult accepted(List<SSBMCharacter> oldMains) {
            return new ToggleMainResult(oldMains, true);
        }
    }
}
