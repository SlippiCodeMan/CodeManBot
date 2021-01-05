package io.fluentcoding.codemanbot.util.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Platforms {
    CHALLONGE("challonge", "https://challonge.com/"),
    SMASHGG("smash.gg", "https://smash.gg/");

    String name;
    String url;
}
