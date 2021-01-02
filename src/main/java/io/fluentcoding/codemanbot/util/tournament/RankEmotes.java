package io.fluentcoding.codemanbot.util.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RankEmotes {
    FIRST(1, "<:first:794868918358769666>"),
    SECOND(2, "<:second:794868918358769666>"),
    THIRD(3, "<:third:794868918358769666>");

    private int number;
    private String emote;
}
