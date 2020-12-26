package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;

import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {

    public static String stringWithSlippiUsername(String prefix, String username) {
        return prefix + " ***(" + username + ")***";
    }

    public static String stringWithMains(String prefix, String mains) {
        return prefix + " ***[ " + mains + " ]***";
    }

    public static String stringWithSlippiUsernameAndMains(String prefix, String username, String mains) {
        return prefix + " ***(" + username + ") [ " + mains + " ]***";
    }

    public static String getMainsFormatted(List<SSBMCharacter> characters) {
        return characters == null ? "" : characters.stream()
                .map(main -> "<:" + main.getName()
                        .replaceAll("\\s+", "_")
                        .replaceAll("[&.-]", "").toLowerCase()
                        + ":" + main.getEmoteId() + ">")
                .collect(Collectors.joining(" "));
    }
}
