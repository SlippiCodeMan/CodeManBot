package io.fluentcoding.codemanbot.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {
    public static Date fromIsoTime(String isoTime) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(isoTime, timeFormatter);
        return Date.from(Instant.from(offsetDateTime));
    }
    public static Date fromLong(long time) {
        return new Date(time);
    }
}
