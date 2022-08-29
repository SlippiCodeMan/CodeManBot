package io.fluentcoding.codemanbot.util.codemancommand;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Getter
public abstract class CodeManCommand {
    private final CommandData data;

    public CodeManCommand(CommandData data) {
        this.data = data;
    }

    public abstract void handle(SlashCommandEvent e);
}
