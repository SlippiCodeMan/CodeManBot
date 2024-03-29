package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.SlippiRankBridge.RankEntry;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtil {
    public static String makeUrlValid(String input) {
        if (!input.startsWith("http://") && !input.startsWith("https://"))
            return "https://" + input;

        return input;
    }
    public static String stripDiscordMarkdown(String input) {
        return input.replaceAll("(\\*|_|`|~|\\\\)", "");
    }
    public static String getPersonPrefixedString(boolean you, String suffix) {
        return (you ? "Your " : "Their ") + suffix;
    }
    /*public static String withSlippiUsername(String prefix, String username) {
        return prefix + " **(" + username + ")**";
    }
    public static String withMains(String prefix, String mains) {
        return prefix + " " + mains;
    }
    public static String withSlippiUsernameAndMains(String prefix, String username, String mains) {
        return withMains(withSlippiUsername(prefix, username), mains);
    }*/
    public static String listItemDetails(String prefix, String username, String mains, RankEntry rank) {
        String result = prefix;

        if (username != null)
            result += " **(" + username + ")**";

        if (mains != null)
            result += " " + mains;

        if (rank != null && rank.hasPlayed())
            result += " **[" + getRankEmoji(rank.getRank()) + " " + rank.getRating() + "]**";

        return result;
    }
    public static String getMainsFormatted(List<SSBMCharacter> characters) {
        return characters == null ? "" : characters.stream()
                .map(main -> "<:" + main.getName()
                        .replaceAll("\\s+", "_")
                        .replaceAll("[&.-]", "").toLowerCase()
                        + ":" + main.getEmoteId() + ">")
                .collect(Collectors.joining(" "));
    }
    public static String getRankEmoji(String rank) {
        String rankEmojiName = rank.replace(" ", "");
        try {
            return Application.JDA.getGuildById(GlobalVar.TEST_SERVER_ID).getEmotesByName(rankEmojiName, true).get(0).getAsMention();
        } catch(IndexOutOfBoundsException e) {
            return null;
        }
    }
    public static String getRankFormatted(RankEntry rank, boolean emptyOnNoRank) {
        return rank == null ?
            (emptyOnNoRank ? "" : "*No rank found*") :
            rank.getRank() + " (" + rank.getRating() + ", " + rank.getWins() + "W/" + rank.getLosses() + "L)";
    }
    public static String getRankImageUrl(RankEntry rank) {
        String rankUrl = rank.getRank().replace(" ", "%20");
        return "http://slprank.com/imgs/" + rankUrl + ".png";
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
    public static String fromatDate(Date date) {
        if (date == null)
            return "";

        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy, hh:mm z");  
        return dateFormat.format(date);
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
    public static String oneLineCodeBlock(int input) {
        return "`" + input + "`";
    }
    public static Map<String, String> separateCodeFromUsername(String input) {
        String username = input.replaceAll("[\\(\\[]?([A-Za-z])+#[0-9]{1,3}[\\]\\)]?$", "").stripTrailing();

        String code = "";
        Matcher matcher = Pattern.compile("([A-Za-z])+#[0-9]{1,3}").matcher(input);
        while (matcher.find())
            code = matcher.group(0).toUpperCase();

        Map<String, String> hm = new HashMap<String, String>();
        hm.put("username", username);
        hm.put("code", code);
        return hm;
    }
    public static String removeHardcodedSeeding(String input) {
        return input.replaceAll("[0-9]+.\\s", "");
    }
    public static String removeUnderscores(String input) {
        return input.replace("_", " ");
    }
}
