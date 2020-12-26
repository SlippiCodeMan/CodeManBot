package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;

import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {

    public static String stringWithSlippiUsername(String prefix, String username) {
        return prefix + " **(" + username + ")**";
    }

    public static String stringWithMains(String prefix, String mains) {
        return prefix + " " + mains;
    }

    public static String stringWithSlippiUsernameAndMains(String prefix, String username, String mains) {
        return prefix + " **(" + username + ")** " + mains ;
    }

    public static String getMainsFormatted(List<SSBMCharacter> characters) {
        return characters == null ? "" : characters.stream()
                .map(main -> "<:" + main.getName()
                        .replaceAll("\\s+", "_")
                        .replaceAll("[&.-]", "").toLowerCase()
                        + ":" + main.getEmoteId() + ">")
                .collect(Collectors.joining(" "));
    }

    public static String getNumberedEmoji(int digit) {
        char number = (char)('\u0030' + digit);
        return number + "\uFE0F\u20E3";
    }

    public static int getDigitOfEmoji(String unicode) {
        String result = String.format("\\u%04x", (int) unicode.charAt(0));
        return Character.codePointAt(result, 6);
    }
}
