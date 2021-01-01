package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Map;

@Getter
public class DeprecatedCodeManCommand extends CodeManCommand {
    private String newCommand;

    public DeprecatedCodeManCommand(String newCommand, String name, String... aliases) {
        super(null, name, aliases);

        this.newCommand = newCommand;
    }

    public void handle(GuildMessageReceivedEvent e, Map<String, String> args) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(GlobalVar.ERROR);
        builder.setDescription("This command is deprecated! Consider using " + StringUtil.bold(Application.EXEC_MODE.getCommandPrefix()) + newCommand + " instead!");

        e.getChannel().sendMessage(builder.build()).queue();
    }

    public String getHelpTitle() {
        return null;
    }
}
