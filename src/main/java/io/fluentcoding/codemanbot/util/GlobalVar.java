package io.fluentcoding.codemanbot.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.awt.Color;

public class GlobalVar {
    public static String FROG_EMOJI = "\uD83D\uDC38",
            ARROW_LEFT_EMOJI = "\u25C0\uFE0F",
            ARROW_RIGHT_EMOJI ="\u25B6\uFE0F",
            LOADING_EMOJI = "<a:loading:792463286951870496>",
            CHECKMARK_EMOJI = "\u2705",
            GREEN_HEART_EMOJI = "\u1F49A",
            CANCEL_EMOJI = "\u274C";

    public static Color SUCCESS = new Color(68, 169, 99),
            ERROR = new Color(168, 67, 104),
            LOADING = new Color(47, 49, 54),
            WAITING = new Color(243, 193, 120);

    public static int MAX_ITEMS_PER_PAGE = 10;

    public static Dotenv dotenv = Dotenv.load();

    public static long[] owners = new long[]{
            582645006100201485L, // ANANAS
            522871749667323956L // FLUENTCODING
    };
}
