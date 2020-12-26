package io.fluentcoding.codemanbot.container;

public enum BroadcastContainer {
    INSTANCE;

    private long currentMessageId = -1;

    public void broadcastHandler(long messageId) {
        currentMessageId = messageId;
    }
    public void stopBroadcast() {
        currentMessageId = -1;
    }
    public long getCurrentMessageId() {return currentMessageId;}
    public boolean broadcastAlreadyActive() {return currentMessageId != -1;}
}
