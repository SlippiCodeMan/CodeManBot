package io.fluentcoding.codemanbot.util;

import java.util.regex.Pattern;

public class PatternChecker {
    private static final Pattern connectCodePattern = Pattern.compile("^([A-Za-z])+#[0-9]{1,3}$"),
        challongeUrlPrefixPattern = Pattern.compile("^(https://|http://)?(www.|.+\\.)?challonge.com");

    public static boolean isConnectCode(String input) {
        return input.length() <= 8 && is(input, connectCodePattern);
    }
    public static boolean isSlippiUsername(String input) {
        return input.length() <= 15;
    }
    public static boolean isChallongeLink(String input) {
        return challongeUrlPrefixPattern.matcher(input).find();
    }
    private static boolean is(String input, Pattern pattern) {
        return pattern.matcher(input).matches();
    }
}
