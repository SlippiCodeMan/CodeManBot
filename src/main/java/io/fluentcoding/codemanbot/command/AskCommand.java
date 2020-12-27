package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class AskCommand extends CodeManCommand {

    public AskCommand(String description, String prefix, String... aliases) {
        super(description, prefix, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e) {
        EmbedBuilder builder = new EmbedBuilder();

        String code = DatabaseBridge.getCode(e.getAuthor().getIdLong());
        if (code == null) {
            builder.setDescription("You haven't connected to CodeMan yet! Take a look at **" + Application.EXEC_MODE.getCommandPrefix() + "connect**!");
            builder.setColor(GlobalVar.ERROR);
        } else {
            builder.setTitle("Netplay Search");
            builder.setDescription(e.getAuthor().getName() + " is looking for an opponent!");
            builder.setThumbnail(e.getAuthor().getAvatarUrl());
            builder.setColor(GlobalVar.SUCCESS);

            builder.addField("Their code", code, false);
            builder.setFooter(GlobalVar.FROG_EMOJI + " slippi 2.x.x");
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
