package io.fluentcoding.codemanbot.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.entities.PrivateChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public boolean isConnecting(long userId) {
        return connectInformation.entrySet().stream().anyMatch(entry -> entry.getKey().getUserId() == userId);
    }

    public void clearConnectInformation() {
        connectInformation.clear();
    }

    public Set<Map.Entry<ConnectInformationKey, PrivateChannel>> getConnectInformationEntries() {
        return connectInformation.entrySet();
    }

    @AllArgsConstructor
    @Data
    public static class ConnectInformationKey {
        private String code;
        private long userId;
    }
}
