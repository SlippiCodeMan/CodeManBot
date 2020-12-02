package io.fluentcoding.codemanbot.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExecutionMode {
    PRODUCTION(System.getenv("CODEMAN_PROD_TOKEN"), "&"),
    DEV(System.getenv("CODEMAN_PROD_TOKEN"), "!");

    private String discordToken;
    private String commandPrefix;
}
