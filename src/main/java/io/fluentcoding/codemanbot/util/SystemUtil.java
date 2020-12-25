package io.fluentcoding.codemanbot.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SystemUtil {
    private static int mb = 1024 * 1024;

    public static MemoryStats memoryStats() {
        Runtime instance = Runtime.getRuntime();

        return new MemoryStats(
                instance.totalMemory() / mb,
                instance.maxMemory() / mb,
                instance.freeMemory() / mb,
                (instance.totalMemory() - instance.freeMemory()) / mb
        );
    }

    @AllArgsConstructor
    @Getter
    public static class MemoryStats {
        private float totalMemory;
        private float maxMemory;
        private float freeMemory;
        private float usedMemory;
    }
}
