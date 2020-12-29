package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.util.GlobalVar;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public abstract class DevCodeManCommand extends RestrictedCodeManCommand {

    public DevCodeManCommand(String name, String... aliases) {
        super((user, guild) -> Arrays.stream(GlobalVar.owners).anyMatch(owner -> user.getIdLong() == owner), "devs only", name, aliases);
    }
}
