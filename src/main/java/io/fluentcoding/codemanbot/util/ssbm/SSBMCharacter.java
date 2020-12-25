package io.fluentcoding.codemanbot.util.ssbm;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum SSBMCharacter {
    DOCTOR_MARIO("Dr. Mario", 792019249329078273L, "doctormario", "doc", "drmario", "drm"),
    MARIO("Mario", 792019249329078273L, "maria"),
    LUIGI("Luigi", 792019249329078273L, "lugi", "luggi", "mariosbro"),
    BOWSER("Bowser", 792019249329078273L, "bowser", "bowsa", "bowza", "bw"),
    PEACH("Peach", 792013225696428052L, "peach", "marioswife"),
    YOSHI("Yoshi", 792013018514325514L, "egg"),
    DK("Donkey Kong", 792019249329078273L, "dk"),
    CAPTAIN_FALCON("Captain Falcon", 792019249329078273L, "cptfalcon", "falcon"),
    GANONDORF("Ganondorf", 792019249329078273L, "ganon", "dorf", "falconbutbad"),
    FALCO("Falco", 792019249329078273L, "bird", "birdo", "falcolombardi"),
    FOX("Fox", 792019249329078273L, "focks", "foxmccloud", "nojohns", "salt", "lilbeach"),
    NESS("Ness", 792019249329078273L),
    ICE_CLIMBERS("Ice Climbers", 792019249329078273L, "iceclimber", "icies", "ics", "ice", "climbers", "climber", "icec", "iclimbers", "iclimber"),
    KIRBY("Kirby", 792019249329078273L, "kirbo", "puyo"),
    SAMUS("Samus", 792013080351473695L),
    ZELDA("Zelda", 792012125094674462L, "sheikbutbad"),
    SHEIK("Sheik", 792013047412948993L),
    LINK("Link", 792019249329078273L, "biglink"),
    YOUNG_LINK("Young Link", 792012968526217216L, "yl", "young", "ylink"),
    PICHU("Pichu", 792013197871153212L, "pikabutbad"),
    PIKACHU("Pikachu", 792013163460689921L, "pika", "chu", "pikapika"),
    JIGGLYPUFF("Jigglypuff", 792019249329078273L, "pummeluff", "puff", "overpowered", "jpuff", "jigglyp"),
    MEWTWO("Mewtwo", 792019249329078273L, "m2", "jason"),
    MRGAMEWATCH("Mr. Game & Watch", 792019249329078273L, "gnw", "gw", "mgnw", "mrgnw", "mrgw", "gameandwatch", "mrgameandwatch", "mrgamewatch", "gamewatch", "mrgamenwatch", "gamenwatch"),
    MARTH("Marth", 792019249329078273L, "longgrab"),
    ROY("Roy", 792013113834340373L, "marthbutbad");

    private String name;
    private long emoteId;
    private List<String> identifiers = new ArrayList<>();

    SSBMCharacter(String name, long emoteId, String... identifiers) {
        this.name = name;
        this.emoteId = emoteId;
        this.identifiers.add(name.toLowerCase().replaceAll("\\s+",""));
        this.identifiers.addAll(Arrays.asList(identifiers));
    }
}