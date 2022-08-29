package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.ActivityUpdater;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Objects;

public class DisconnectCommand extends CodeManCommand {

    public DisconnectCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        DatabaseBridge.removeData(Objects.requireNonNull(e.getMember()).getIdLong());
        ActivityUpdater.update(e.getJDA());

        e.reply("Your data has been removed successfully.").setEphemeral(true).queue();
    }
}
