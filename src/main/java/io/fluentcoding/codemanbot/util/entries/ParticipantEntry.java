package io.fluentcoding.codemanbot.util.entries;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ParticipantEntry {
    private String name;
    private String connectCode;
    private Boolean checkedIn;
    private Integer seed;
    private Integer placement;
    private Boolean isPlacementFinal;
}