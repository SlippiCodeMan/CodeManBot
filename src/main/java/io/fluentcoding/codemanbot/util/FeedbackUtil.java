package io.fluentcoding.codemanbot.util;

public class FeedbackUtil {
    public final static String NOTCONNECTED = "You haven't connected to CodeMan yet! Take a look at "
            + StringUtil.bold("/connect") +
            " !";
    public final static String ALREADYCONNECTED  = "You are already connected to this code!";
    public final static String ISCONNECTING = "You are already verifying a code!";
}