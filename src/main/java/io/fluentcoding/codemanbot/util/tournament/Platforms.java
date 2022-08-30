package io.fluentcoding.codemanbot.util.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@AllArgsConstructor
@Getter
public enum Platforms {
    CHALLONGE("Challonge", "https://challonge.com/", new Color(255, 115, 36)),
    SMASHGG("Smash.gg", "https://smash.gg/", new Color(200, 59, 61));

    String name;
    String url;
    Color color;
}
