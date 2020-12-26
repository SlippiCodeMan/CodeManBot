package io.fluentcoding.codemanbot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternChecker {
    private static final Pattern connectCodePattern = Pattern.compile("^([A-Za-z])+#[0-9]{1,3}$");
    private static final Pattern urlPattern = Pattern.compile("\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");

    public static boolean isConnectCode(String input) {
        return input.length() <= 8 && is(input, connectCodePattern);
    }
    public static boolean isSlippiUsername(String input) {
        return input.length() <= 15;
    }
    public static String fetchLastUrlFromInput(String input) {
        Matcher m = urlPattern.matcher(input);
        String result = "";

        while(m.find()) {
            result = m.group();
        }
        if (result.startsWith("(") && result.endsWith(")"))
        {
            result = result.substring(1, result.length() - 1);
        }

        return result;
    }

    private static boolean is(String input, Pattern pattern) {
        return pattern.matcher(input).matches();
    }
}
