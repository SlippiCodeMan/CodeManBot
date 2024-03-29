package io.fluentcoding.codemanbot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternChecker {
    private static final Pattern connectCodePattern = Pattern.compile("^([A-Za-z0-9])+#[0-9]{1,6}$"),
        challongeUrlPrefixPattern = Pattern.compile("^(https://|http://)?(www.|.+\\.)?challonge.com"),
        subdomainPrefixPattern = Pattern.compile("^[^.]*(?=\\.\\w+\\..+$)"),
        hexColorPattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    public static boolean isConnectCode(String input) {
        return input.length() <= 8 && is(input, connectCodePattern);
    }
    public static boolean isSlippiUsername(String input) {
        return input.length() <= 15;
    }
    public static boolean isChallongeLink(String input) {
        return challongeUrlPrefixPattern.matcher(input).find();
    }
    public static boolean isHexColorPattern(String input) {
        return is(input, hexColorPattern);
    }
    public static String getSubdomain(String input) {
        int pos = 0;
        if (input.startsWith("http://") || input.startsWith("https://")) {
            pos = input.indexOf('/') + 2;
        }

        Matcher m = subdomainPrefixPattern.matcher(input.substring(pos));
        if (!m.find())
            return null;
        return m.group();
    }
    private static boolean is(String input, Pattern pattern) {
        return pattern.matcher(input).matches();
    }
}
