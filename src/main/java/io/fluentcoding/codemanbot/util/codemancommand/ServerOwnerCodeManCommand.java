package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.util.GlobalVar;

import java.util.Arrays;

public abstract class ServerOwnerCodeManCommand extends RestrictedCodeManCommand {
    public ServerOwnerCodeManCommand(String name, String... aliases) {
        super((user, guild) -> guild.retrieveOwner().complete().getIdLong() == user.getIdLong(), "server owner only", name, aliases);
    }
}
