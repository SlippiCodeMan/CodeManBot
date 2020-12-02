package io.fluentcoding.codemanbot.util;

import java.util.regex.Pattern;

public class PatternChecker {
    private static Pattern connectCodePattern  = Pattern.compile("^([A-Za-z])+#([0-9]{3}|[0-9])$"),
                            slippiUsernamePattern = Pattern.compile("^[A-Za-z0-9$&+,:;=?@#|'<>.^*()%! -]{1,15}$");

    public static boolean isConnectCode(String input) {
        return is(input, connectCodePattern);
    }
    public static boolean isSlippiUsername(String input) {
        return is(input, slippiUsernamePattern);
    }

    private static boolean is(String input, Pattern pattern) {
        return pattern.matcher(input).matches();
    }
}
