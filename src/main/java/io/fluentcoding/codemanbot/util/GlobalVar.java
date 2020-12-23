package io.fluentcoding.codemanbot.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.awt.Color;

public class GlobalVar {
    public static String FROG_EMOJI = "\uD83D\uDC38";

    public static Color SUCCESS = new Color(68, 169, 99),
            ERROR = new Color(168, 67, 104),
            LOADING = new Color(125, 100, 0);

    public static Dotenv dotenv = Dotenv.load();
}
