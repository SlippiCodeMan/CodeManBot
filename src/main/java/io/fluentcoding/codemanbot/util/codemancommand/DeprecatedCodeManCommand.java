package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.util.GlobalVar;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;

@Getter
public class DeprecatedCodeManCommand extends CodeManCommand {
    String newCommand;

    public DeprecatedCodeManCommand(String newCommand, String description, String name, String... aliases) {
        super(description, name, aliases);

        this.newCommand = newCommand;
    }

    public void handle(MessageReceivedEvent e) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(GlobalVar.ERROR);
        builder.setDescription("This command is deprecated! Take a look at **" + Application.EXEC_MODE.getCommandPrefix() + newCommand + "**!");

        e.getChannel().sendMessage(builder.build()).queue();
    }

    public String getHelpTitle() {
        return null;
    }
}
