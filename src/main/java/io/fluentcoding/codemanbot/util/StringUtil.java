package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {

    public static String getPersonPrefixedString(boolean you, String suffix) {
        return (you ? "Your " : "Their ") + suffix;
    }
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
        return Character.getNumericValue(result.charAt(5));
    }
    public static String getTextFromHtml(String input) {
        return input.replaceAll("<(.|\n)*?>", "");
    }
    public static String formatIsoDateAndTime(String input) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(input, timeFormatter);
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy at hh:mm z");  
        return dateFormat.format(Date.from(Instant.from(offsetDateTime)));
    }
    public static String bold(String input) {
        return "**" + input + "**";
    }
    public static String bold(double input) {
        return "**" + input + "**";
    }
    public static String bold(long input) {
        return "**" + input + "**";
    }
    public static String italic(String input) {
        return "*" + input + "*";
    }
    public static String underline(String input) {
        return "__" + input + "__";
    }
    public static String oneLineCodeBlock(String input) {
        return "`" + input + "`";
    }
}
