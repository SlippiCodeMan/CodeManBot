package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class CodeManCommand {
    private final CommandData data;

    public CodeManCommand(CommandData data) {
        this.data = data;
    }

    public abstract void handle(SlashCommandEvent e);
}
