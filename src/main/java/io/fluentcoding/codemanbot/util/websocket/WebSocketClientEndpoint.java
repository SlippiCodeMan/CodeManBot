package io.fluentcoding.codemanbot.util.websocket;

import org.json.JSONException;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public
class WebSocketClientEndpoint {
    public Session userSession = null;
    private MessageHandler messageHandler;

    public WebSocketClientEndpoint(String endpoint) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(endpoint));
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
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(CloseReason reason) {
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