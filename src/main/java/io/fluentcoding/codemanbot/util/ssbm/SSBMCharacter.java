package io.fluentcoding.codemanbot.util.ssbm;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum SSBMCharacter {
    DOCTOR_MARIO("Dr. Mario","doctormario", "doc", "drmario", "drm"),
    MARIO("Mario", "maria", "doc"),
    LUIGI("Luigi", "luigi", "luggi", "mariosbro"),
    BOWSER("Bowser", "bowser", "bowsa", "bowza", "bw"),
    PEACH("Peach", "peach", "marioswife"),
    YOSHI("Yoshi", "egg"),
    DK("Donkey Kong", "dk"),
    CAPTAIN_FALCON("Captain Falcon", "cptfalcon", "falcon"),
    GANONDORF("Ganondorf", "ganon", "dorf", "falconbutbad"),
    FALCO("Falco", "bird", "birdo", "falcolombardi"),
    FOX("Fox", "focks", "foxmccloud", "nojohns"),
    NESS("Ness"),
    ICE_CLIMBERS("Ice Climbers", "iceclimber", "icies", "ics", "ice", "climbers", "climber", "icec", "iclimbers", "iclimber"),
    KIRBY("Kirby", "kirbo", "puyo"),
    SAMUS("Samus"),
    ZELDA("Zelda", "sheikbutbad"),
    SHEIK("Sheik"),
    LINK("Link", "biglink"),
    YOUNG_LINK("Young Link", "yl", "young", "ylink"),
    PICHU("Pichu", "pikabutbad"),
    PIKACHU("Pikachu", "pika", "chu", "pikapika"),
    JIGGLYPUFF("Jigglypuff", "pummeluff", "puff", "overpowered", "jpuff", "jigglyp"),
    MEWTWO("Mewtwo", "m2", "jason"),
    MRGAMEWATCH("Mr. Game & Watch", "gnw", "gw", "mgnw", "mrgnw", "mrgw", "gameandwatch", "mrgameandwatch", "mrgamewatch", "gamewatch", "mrgamenwatch", "gamenwatch"),
    MARTH("Marth", "longgrab"),
    ROY("Roy", "marthbutbad");

    private String name;
    private List<String> identifiers;

    SSBMCharacter(String name, String... identifiers) {
        this.name = name;
        this.identifiers.add(name.toLowerCase().replaceAll("\\s+",""));
        this.identifiers.addAll(Arrays.asList(identifiers));
    }
}