package io.fluentcoding.codemanbot.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.entities.PrivateChannel;

import java.util.HashMap;
import java.util.Map;

public enum ConnectContainer {
    INSTANCE;

    private Map<ConnectInformationKey, PrivateChannel> connectInformation = new HashMap<>();

    public void addConnectInformation(ConnectInformationKey information) {
        connectInformation.put(information, null);
    }
    public void setPrivateChannel(ConnectInformationKey information, PrivateChannel channel) {
        connectInformation.put(information, channel);
    }
    public void removeConnectInformation(ConnectInformationKey information) {
        connectInformation.remove(information);
    }
    public PrivateChannel getPrivateChannel(ConnectInformationKey information) {
        return connectInformation.get(information);
    }
    public boolean isConnecting(ConnectInformationKey information) {
        return connectInformation.containsKey(information);
    }

    @AllArgsConstructor
    @Data
    public static class ConnectInformationKey {
        private String code;
        private long userId;
    }
}
