package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.util.GlobalVar;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@Getter
public class DeprecatedCodeManCommand extends CodeManCommand {
    public DeprecatedCodeManCommand(String newCommand, String name, String... aliases) {
        super(null, name, aliases);

        this.newCommand = newCommand;
    }

    public void handle(GuildMessageReceivedEvent e) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(GlobalVar.ERROR);
        builder.setDescription("This command is deprecated! Consider using **" + Application.EXEC_MODE.getCommandPrefix() + newCommand + "** instead!");

        e.getChannel().sendMessage(builder.build()).queue();
    }

    public String getHelpTitle() {
        return null;
    }

    private String newCommand;
}
