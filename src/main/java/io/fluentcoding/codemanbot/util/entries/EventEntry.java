package io.fluentcoding.codemanbot.util.entries;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventEntry {
    private String name;
    private long startAt;
    private EventState state;
    private List<ParticipantEntry> standings;

    @AllArgsConstructor
    @Getter
    public enum EventState {
        CREATED("Seeding"),
        ACTIVE("In Progress"),
        COMPLETED("Finished");

        private String header;

        public static EventState safeValueOf(String name) {
            try {
                return valueOf(name);
            } catch(IllegalArgumentException e) {
                return CREATED;
            }
        }
    }
}