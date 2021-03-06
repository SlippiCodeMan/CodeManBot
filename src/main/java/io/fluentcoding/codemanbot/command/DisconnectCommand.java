package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.ActivityUpdater;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Map;

public class DisconnectCommand extends CodeManCommand {

    public DisconnectCommand(String description, String name, String... aliases) {
        super(description, name, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e, Map<String, String> args) {
        DatabaseBridge.removeData(e.getAuthor().getIdLong());
        ActivityUpdater.update(e.getJDA());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(GlobalVar.SUCCESS);
        builder.setDescription("Your data has been removed successfully!");

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
