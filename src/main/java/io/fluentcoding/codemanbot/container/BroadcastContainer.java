package io.fluentcoding.codemanbot.container;

import io.fluentcoding.codemanbot.command.BroadcastCommand;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

@Getter
public enum BroadcastContainer {
    INSTANCE;

    private long channelId = -1;
    private long currentMessageId = -1;
    @Setter
    private String message = null;
    @Setter
    private String imageLink = null;
    @Setter
    private List<User> cachedTarget = null;
    private BroadcastCommand.BroadcastMode mode = null;

    public void broadcastHandler(long channelId, long messageId) {
        this.channelId = channelId;
        this.currentMessageId = messageId;
    }
    public void setBroadcastMode(BroadcastCommand.BroadcastMode mode) {
        this.mode = mode;
    }
    public void stopBroadcast() {
        this.currentMessageId = -1;
        this.message = null;
        this.imageLink = null;
        this.cachedTarget = null;
    }
    public boolean broadcastAlreadyActive() {return currentMessageId != -1;}
}
