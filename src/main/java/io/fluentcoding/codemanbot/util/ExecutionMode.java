package io.fluentcoding.codemanbot.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExecutionMode {
    PRODUCTION(GlobalVar.dotenv.get("CODEMAN_PROD_TOKEN"), "&"),
    DEV(GlobalVar.dotenv.get("CODEMAN_DEV_TOKEN"), "!");

    private String discordToken;
    private String commandPrefix;
}
