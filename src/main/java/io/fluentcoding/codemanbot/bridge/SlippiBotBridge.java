package io.fluentcoding.codemanbot.bridge;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.container.ConnectContainer;
import io.fluentcoding.codemanbot.util.ActivityUpdater;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import org.json.JSONException;
import org.json.JSONObject;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

public class SlippiBotBridge {
    private static WebsocketClientEndpoint clientEndPoint = null;

    static {
        try {
            clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:9002"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void initHandler() {
        // add listener
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
                    builder.setDescription("Please verify that your code is " + StringUtil.bold(result.getString("botCode")) + " by connecting to **AUTH#999** with your slippi account!");
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
        payload.append("type", "queue");
        payload.append("discordId", information.getUserId());
        payload.append("userCode", information.getCode());
        payload.append("timeout", 300_000); // 5 minutes

        clientEndPoint.sendMessage(payload.toString());
    }

}

@ClientEndpoint
class WebsocketClientEndpoint {

    Session userSession = null;
    private MessageHandler messageHandler;

    public WebsocketClientEndpoint(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket");
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) throws JSONException, InterruptedException {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) throws IOException {
        this.userSession.getBasicRemote().sendText(message);
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public static interface MessageHandler {

        public void handleMessage(String message) throws JSONException, InterruptedException;
    }
}
