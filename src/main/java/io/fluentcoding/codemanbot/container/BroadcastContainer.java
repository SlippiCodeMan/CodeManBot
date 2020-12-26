package io.fluentcoding.codemanbot.container;

import lombok.Getter;

@Getter
public enum BroadcastContainer {
    INSTANCE;

    private long initiatorMessageId = -1;
    private long currentMessageId = -1;

    public void broadcastHandler(long initiatorMessageId, long messageId) {
        this.initiatorMessageId = initiatorMessageId;
        this.currentMessageId = messageId;
    }
    public void stopBroadcast() {
        currentMessageId = -1;
    }
    public boolean broadcastAlreadyActive() {return currentMessageId != -1;}
}
