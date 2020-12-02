package io.fluentcoding.codemanbot.bridge;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import lombok.Data;
import org.bson.Document;

public class DatabaseBridge {
    private final static String mongoUri = System.getenv("CODEMAN_DB_URI");

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

    /*** Returns the old name or null, if not exists ***/
    public static String insertName(long discordId, String name) {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            BasicDBObject filter = new BasicDBObject("discord_id", discordId);
            FindIterable<Document> result = codeManCollection.find(filter);
            if (result.cursor().hasNext()) {
                String oldName = result.cursor().next().getString("slippi_username");
                codeManCollection.updateOne(filter, Updates.set("slippi_username", name));
                return oldName;
            } else {
                codeManCollection.insertOne(new Document("discord_id", discordId).append("slippi_username", name));

                return null;
            }
        }
    }

    public static String getCode(String name) {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            for (Document result : codeManCollection.find(new BasicDBObject("slippi_username", name))) {
                if (result.containsKey("slippi_code")) {
                    return (String) result.get("slippi_code");
                } else {
                    return null;
                }
            }

            return null;
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

    public static String getName(String code) {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            for (Document result : codeManCollection.find(new BasicDBObject("slippi_code", code))) {
                if (result.containsKey("slippi_username")) {
                    return (String) result.get("slippi_username");
                } else {
                    return null;
                }
            }

            return null;
        }
    }

    public static String getName(long discordId) {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            for (Document result : codeManCollection.find(new BasicDBObject("discord_id", discordId))) {
                if (result.containsKey("slippi_username")) {
                    return (String) result.get("slippi_username");
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

    public static long getDiscordIdFromUsername(String username) {
        try (MongoClient client = MongoClients.create(mongoUri)) {
            MongoCollection<Document> codeManCollection = getCollection(client);

            for (Document result : codeManCollection.find(new BasicDBObject("slippi_username", username))) {
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
        private final boolean isAccepted, firstCreation;

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
}
