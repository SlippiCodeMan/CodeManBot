package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.CodeManCommand;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RemoveCommand extends CodeManCommand {

    public RemoveCommand(String description, String name, String... aliases) {
        super(description, name, aliases);
    }

    @Override
    public void handle(MessageReceivedEvent e) {
        DatabaseBridge.removeData(e.getAuthor().getIdLong());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(GlobalVar.SUCCESS);
        builder.setDescription("Your data has been removed successfully!");

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
