package io.fluentcoding.codemanbot.util.ssbm;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum SSBMCharacter {
    DOCTOR_MARIO("Dr. Mario", 791703555580100608L, "doctormario", "doc", "drmario", "drm"),
    MARIO("Mario", 791703555220045826L, "maria"),
    LUIGI("Luigi", 791703555567779852L, "lugi", "luggi", "mariosbro"),
    BOWSER("Bowser", 791703555517055016L, "bowsa", "bowza", "bw"),
    PEACH("Peach", 791703555471310858L, "peach", "turnip"),
    YOSHI("Yoshi", 791703555136421889L, "egg"),
    DK("Donkey Kong", 791703555631218731L, "dk"),
    CAPTAIN_FALCON("Captain Falcon", 791703555455189022L, "cptfalcon", "falcon", "cfalcon", "bond"),
    GANONDORF("Ganondorf", 791703555349807125L, "ganon", "dorf"),
    FALCO("Falco", 791703555794403338L, "bird", "birdo", "falcolombardi"),
    FOX("Fox", 791703556121034762L, "focks", "foxmccloud"),
    NESS("Ness", 791703555601989682L),
    ICE_CLIMBERS("Ice Climbers", 791703555551133736L, "iceclimber", "icies", "ics", "ice", "climbers", "climber", "icec", "iclimbers", "iclimber", "wobbling"),
    KIRBY("Kirby", 791703555572498432L, "kirbo", "puyo", "poyo"),
    SAMUS("Samus", 791703555349676033L),
    ZELDA("Zelda", 791703555362258944L),
    SHEIK("Sheik", 791703555416260638L, "shiek"),
    LINK("Link", 791703555450863666L, "linkle"),
    YOUNG_LINK("Young Link", 791703555463053332L, "yl", "young", "ylink"),
    PICHU("Pichu", 791703555358195714L, "pikabutbad"),
    PIKACHU("Pikachu", 791703555445882920L, "pika", "chu", "pikapika", "yellowrat"),
    JIGGLYPUFF("Jigglypuff", 791703555446800394L, "pummeluff", "puff", "overpowered", "jpuff", "jigglyp", "clutchbox"),
    MEWTWO("Mewtwo", 791703555462922251L, "m2", "jason"),
    MRGAMEWATCH("Mr. Game & Watch", 791703555492544582L, "mistergameandwatch", "mistergamewatch", "mistergnw", "mistergw", "gnw", "gw", "mgnw", "mrgnw", "mrgw", "gameandwatch", "mrgameandwatch", "gamewatch", "mrgamenwatch", "gamenwatch"),
    MARTH("Marth", 791703555437756426L, "longgrab", "zain"),
    ROY("Roy", 791703555529900033L, "cool"),
    WOLF("Wolf", 802358306415837215L, "wulf");

    private final String name;
    private final long emoteId;
    private final List<String> identifiers = new ArrayList<>();

    SSBMCharacter(String name, long emoteId, String... identifiers) {
        this.name = name;
        this.emoteId = emoteId;
        this.identifiers.add(name.toLowerCase().replaceAll("[\\s-`.&]+",""));
        this.identifiers.addAll(Arrays.asList(identifiers));
    }
}
