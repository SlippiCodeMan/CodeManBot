package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class LobbyCommand extends CodeManCommand {

    public LobbyCommand(String description, String prefix, String... aliases) {
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
            e.getMessage().delete().queue();

            builder.setAuthor(e.getAuthor().getName(), null, e.getAuthor().getAvatarUrl());
            builder.setTitle("Friendlies Singles `[1/2]`");
            builder.setDescription("a few games");
            builder.setColor(GlobalVar.WAITING);

            List<SSBMCharacter> characters = DatabaseBridge.getMains(e.getAuthor().getIdLong());
            if (characters != null && characters.size() != 0) {
                builder.addField("Mains", StringUtil.getMainsFormatted(characters), true);
            }

            builder.setFooter("Lobby ID: 1824");
        }
        e.getChannel().sendMessage(builder.build()).queue(message -> message.addReaction(GlobalVar.ARROW_RIGHT_EMOJI));
    }
}
