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

    private Bandwidth limit = Bandwidth.classic(3, Refill.intervally(5, Duration.ofSeconds(5)));
    private Map<Long, Bucket> userBuckets = new HashMap<>();

    public boolean userAllowedToAction(Long userId) {
        Bucket bucket = userBuckets.get(userId);

        if (bucket == null) {
            bucket = Bucket4j.builder().addLimit(limit).build();
            userBuckets.put(userId, bucket);
        }

        System.out.println(userId + " " + bucket.getAvailableTokens());

        return bucket.tryConsume(1);
    }
}
