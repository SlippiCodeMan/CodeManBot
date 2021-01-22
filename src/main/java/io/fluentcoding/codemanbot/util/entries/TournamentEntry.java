package io.fluentcoding.codemanbot.util.entries;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TournamentEntry {
    private String name;
    private String description;
    private String imageProfile;
    private String imageBanner;
    private String gameName;
    private String type;
    private Date startAt;
    private String state;
    private Integer participantsCount;
    private List<EventEntry> events;
    private List<ParticipantEntry> participants;
}