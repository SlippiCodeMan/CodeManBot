package io.fluentcoding.codemanbot.util.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RankEmotes {
    FIRST(1, "<:first:794868918358769666>"),
    SECOND(2, "<:second:794868918522871868>"),
    THIRD(3, "<:third:794868918652502026>");

    private int number;
    private String emote;
}
