package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.bridge.SlippiBridge;

public class StringUtil {

    public static String stringWithSlippiUsername(String prefix, String username) {
        return prefix + " ***(" + username + "***)";
    }
}
