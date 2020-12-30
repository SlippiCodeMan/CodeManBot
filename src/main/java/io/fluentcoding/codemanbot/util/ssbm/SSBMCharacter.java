package io.fluentcoding.codemanbot.util.ssbm;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum SSBMCharacter {
    DOCTOR_MARIO("Dr. Mario", 791703555580100608L, "doctormario", "doc", "drmario", "drm"),
    MARIO("Mario", 791703555220045826L, "maria", "papi nulo"),
    LUIGI("Luigi", 791703555567779852L, "lugi", "luggi", "mariosbro"),
    BOWSER("Bowser", 791703555517055016L, "bowser", "bowsa", "bowza", "bw"),
    PEACH("Peach", 791703555471310858L, "peach", "marioswife", "wife"),
    YOSHI("Yoshi", 791703555136421889L, "egg", "thicc"),
    DK("Donkey Kong", 791703555631218731L, "dk", "donkey long"),
    CAPTAIN_FALCON("Captain Falcon", 791703555455189022L, "cptfalcon", "falcon", "falconono", "wizzi fan", "bond", "mr tas"),
    GANONDORF("Ganondorf", 791703555349807125L, "ganon", "dorf", "falconbutbad"),
    FALCO("Falco", 791703555794403338L, "bird", "birdo", "falcolombardi", "flacko", "kfc"),
    FOX("Fox", 791703556121034762L, "focks", "foxmccloud", "nojohns", "salt", "lil beach", "otah"),
    NESS("Ness", 791703555601989682L),
    ICE_CLIMBERS("Ice Climbers", 791703555551133736L, "iceclimber", "icies", "ics", "ice", "climbers", "climber", "icec", "iclimbers", "iclimber", "woobling"),
    KIRBY("Kirby", 791703555572498432L, "kirbo", "puyo", "poyo"),
    SAMUS("Samus", 791703555349676033L),
    ZELDA("Zelda", 791703555362258944L, "sheikbutbad", "electro-girl", "magicana"),
    SHEIK("Sheik", 791703555416260638L, "shiek", "techchase master"),
    LINK("Link", 791703555450863666L, "biglink"),
    YOUNG_LINK("Young Link", 791703555463053332L, "yl", "young", "ylink", "young lean"),
    PICHU("Pichu", 791703555358195714L, "pikabutbad", "smol pikachu"),
    PIKACHU("Pikachu", 791703555445882920L, "pika", "chu", "pikapika", "yellow rat", "pichu xl"),
    JIGGLYPUFF("Jigglypuff", 791703555446800394L, "pummeluff", "puff", "overpowered", "jpuff", "jigglyp", "clutchbox"),
    MEWTWO("Mewtwo", 791703555462922251L, "m2", "jason"),
    MRGAMEWATCH("Mr. Game & Watch", 791703555492544582L, "gnw", "gw", "mgnw", "mrgnw", "mrgw", "gameandwatch", "mrgameandwatch", "mrgamewatch", "gamewatch", "mrgamenwatch", "gamenwatch"),
    MARTH("Marth", 791703555437756426L, "longgrab", "ken", "zain"),
    ROY("Roy", 791703555529900033L, "marthbutbad");

    private final String name;
    private final long emoteId;
    private final List<String> identifiers = new ArrayList<>();

    SSBMCharacter(String name, long emoteId, String... identifiers) {
        this.name = name;
        this.emoteId = emoteId;
        this.identifiers.add(name.toLowerCase().replaceAll("\\s+",""));
        this.identifiers.addAll(Arrays.asList(identifiers));
    }
}