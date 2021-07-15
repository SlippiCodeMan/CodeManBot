package io.fluentcoding.codemanbot.bridge;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.container.ConnectContainer;
import io.fluentcoding.codemanbot.util.ActivityUpdater;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.websocket.WebSocketClientEndpoint;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SlippiBotBridge {
    private static WebSocketClientEndpoint clientEndPoint = null;

    static {
        try {
            clientEndPoint = new WebSocketClientEndpoint("ws://localhost:9002");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void reconnect() {
        clientEndPoint.connect();
    }

    public static boolean isConnected() {
        return clientEndPoint.userSession != null;
    }

    public static void initHandler() {
        // add listeners
        clientEndPoint.addCloseHandler(() -> {
            for (var entry : ConnectContainer.INSTANCE.getConnectInformationEntries()) {
                EmbedBuilder builder = new EmbedBuilder();

                builder.setDescription("We lost the connection to our bot service! Please try again later.");
                builder.setColor(GlobalVar.ERROR);
                entry.getValue().sendMessage(builder.build()).queue();
            }
            ConnectContainer.INSTANCE.clearConnectInformation();
        });

        clientEndPoint.addMessageHandler(message -> {
            System.out.println(message);
            JSONObject result = new JSONObject(message);
            String type = result.getString("type");

            ConnectContainer.ConnectInformationKey information = null;
            PrivateChannel userChannel = null;

            if (result.has("userCode") && result.has("discordId")) {
                information = new ConnectContainer.ConnectInformationKey(result.getString("userCode"), result.getLong("discordId"));
                userChannel = ConnectContainer.INSTANCE.getPrivateChannel(information);
            }

            if (userChannel == null)
                return;

            EmbedBuilder builder = new EmbedBuilder();
            switch(type) {
                case "slippiErr":
                    builder.setDescription("There was an error with our bots connecting to the slippi servers!\n");
                    builder.appendDescription("Please try again later or reach out to one of our admins.");
                    builder.setColor(GlobalVar.ERROR);
                    ConnectContainer.INSTANCE.removeConnectInformation(information);
                    break;
                case "noReadyClient":
                    builder.setDescription("We don't have any available bots right now!\n");
                    builder.appendDescription("Please try again later or reach out to one of our admins.");
                    builder.setColor(GlobalVar.ERROR);
                    ConnectContainer.INSTANCE.removeConnectInformation(information);
                    break;
                case "searching":
                    builder.setDescription("Please verify that your code is " + StringUtil.bold(information.getCode()) + " by connecting to " + StringUtil.bold(result.getString("botCode")) + " with your slippi account!");
                    builder.setFooter("You have 5 minutes left!");
                    builder.setColor(GlobalVar.WAITING);
                    break;
                case "authenticated":
                    builder.setDescription("You have been verified!");
                    builder.setColor(GlobalVar.SUCCESS);
                    ConnectContainer.INSTANCE.removeConnectInformation(information);
                    DatabaseBridge.insertCode(information.getUserId(), information.getCode());

                    Application.JDA.awaitReady();
                    ActivityUpdater.update(Application.JDA);
                    break;
                case "timeout":
                    builder.setDescription("You were too late and didn't verify!");
                    builder.setColor(GlobalVar.ERROR);
                    ConnectContainer.INSTANCE.removeConnectInformation(information);
                    break;
            }

            userChannel.sendMessage(builder.build()).queue();
        });
    }

    public static void sendQueue(ConnectContainer.ConnectInformationKey information) throws JSONException, IOException {
        JSONObject payload = new JSONObject();
        payload.put("type", "queue");
        payload.put("discordId", information.getUserId());
        payload.put("userCode", information.getCode());
        payload.put("timeout", 300_000); // 5 minutes

        clientEndPoint.sendMessage(payload.toString());
    }

}
