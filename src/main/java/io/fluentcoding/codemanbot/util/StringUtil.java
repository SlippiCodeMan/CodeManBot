package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.bridge.SlippiBridge;

public class StringUtil {

    public static String codeWithActualName(SlippiBridge.UserEntry userEntry) {
        return userEntry.getCode() +
                (userEntry.getDisplayName() != null ? " ***(" + userEntry.getDisplayName() + "***)" : "");
    }
}
