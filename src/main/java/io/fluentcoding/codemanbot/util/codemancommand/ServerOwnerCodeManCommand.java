package io.fluentcoding.codemanbot.util.codemancommand;

import net.dv8tion.jda.api.Permission;

public abstract class ServerOwnerCodeManCommand extends RestrictedCodeManCommand {

    public ServerOwnerCodeManCommand(String name, String... aliases) {
        super((member, guild) -> member.getPermissions().contains(Permission.ADMINISTRATOR), "server admin only", name, aliases);
    }
}
