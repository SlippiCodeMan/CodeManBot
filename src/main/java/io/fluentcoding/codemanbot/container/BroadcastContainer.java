package io.fluentcoding.codemanbot.container;

import io.fluentcoding.codemanbot.command.BroadcastCommand;
import lombok.Getter;

@Getter
public enum BroadcastContainer {
    INSTANCE;

    private long channelId = -1;
    private long currentMessageId = -1;
    private BroadcastCommand.BroadcastMode mode = null;

    public void broadcastHandler(long channelId, long messageId) {
        this.channelId = channelId;
        this.currentMessageId = messageId;
    }
    public void setBroadcastMode(BroadcastCommand.BroadcastMode mode) {
        this.mode = mode;
    }
    public void stopBroadcast() {
        currentMessageId = -1;
    }
    public boolean broadcastAlreadyActive() {return currentMessageId != -1;}
}
