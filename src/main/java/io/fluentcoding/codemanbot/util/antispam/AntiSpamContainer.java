package io.fluentcoding.codemanbot.util.antispam;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public enum AntiSpamContainer {
    INSTANCE;

    private Bandwidth limit = Bandwidth.simple(5, Duration.ofSeconds(10));
    private Map<Long, Bucket> userBuckets = new HashMap<>();

    public boolean userAllowedToAction(Long userId) {
        Bucket bucket = userBuckets.get(userId);

        if (bucket == null) {
            bucket = Bucket4j.builder().addLimit(limit).build();
            userBuckets.put(userId, bucket);
        }

        return bucket.tryConsume(1);
    }
}
