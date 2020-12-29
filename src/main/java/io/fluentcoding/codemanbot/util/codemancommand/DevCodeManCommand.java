package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.util.GlobalVar;

import java.util.Arrays;

public abstract class DevCodeManCommand extends RestrictedCodeManCommand {

    public DevCodeManCommand(String name, String... aliases) {
        super((user, guild) -> Arrays.stream(GlobalVar.owners).anyMatch(owner -> user.getIdLong() == owner), "devs only", name, aliases);
    }
}
