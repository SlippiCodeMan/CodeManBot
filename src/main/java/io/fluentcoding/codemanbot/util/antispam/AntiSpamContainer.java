package io.fluentcoding.codemanbot.util.antispam;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public enum AntiSpamContainer {
    INSTANCE;

    private final Bandwidth minuteLimit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofSeconds(10)));
    private final Bandwidth limit = Bandwidth.classic(20, Refill.intervally(10, Duration.ofSeconds(60)));
    private Map<Long, Bucket> userBuckets = new HashMap<>();

    public boolean userAllowedToAction(Long userId) {
        Bucket bucket = userBuckets.get(userId);

        if (bucket == null) {
            bucket = Bucket4j.builder().addLimit(minuteLimit).addLimit(limit).build();
            userBuckets.put(userId, bucket);
        }

        return bucket.tryConsume(1);
    }
}
