package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.stream.Collectors;

public class AskCommand extends CodeManCommand {

    public AskCommand(String description, String prefix, String... aliases) {
        super(description, prefix, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Netplay Search");
        builder.setDescription(e.getAuthor().getName() + " is looking for an opponent!");
        builder.setThumbnail(e.getAuthor().getAvatarUrl());
        builder.setColor(GlobalVar.SUCCESS);

        builder.addField("Their code", DatabaseBridge.getCode(e.getAuthor().getIdLong()), false);
        builder.setFooter(GlobalVar.FROG_EMOJI + " slippi 2.x.x");

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
