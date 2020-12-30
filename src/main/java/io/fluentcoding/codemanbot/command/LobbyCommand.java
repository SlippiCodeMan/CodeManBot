package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.EmbedUtil;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.Random;

public class LobbyCommand extends CodeManCommand {

    public LobbyCommand(String description, String prefix, String... aliases) {
        super(description, prefix, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e) {
        // Set settings
        // WIP
        EmbedBuilder lobby = new EmbedBuilder();

        String code = DatabaseBridge.getCode(e.getAuthor().getIdLong());
        if (code == null) {
            lobby = EmbedUtil.notConnected(lobby);
        } else {
            e.getMessage().delete().queue();

            EmbedBuilder stepOne = new EmbedBuilder();
            stepOne.setTitle("Lobby Creator");
            stepOne.setDescription("Enter the description of your lobby:");

            String content = e.getMessage().getContentRaw();
            if (!content.isEmpty()) {
                lobby.setDescription(content);
                e.getJDA().removeEventListener(this); // stop listening
            }

            lobby.setAuthor(e.getAuthor().getName(), null, e.getAuthor().getAvatarUrl());
            lobby.setTitle("Searching `[1/2]`");
            lobby.setDescription(StringUtil.bold("quick sesh"));
            lobby.setColor(GlobalVar.WAITING);

            List<SSBMCharacter> characters = DatabaseBridge.getMains(e.getAuthor().getIdLong());
            if (characters != null && characters.size() != 0) {
                lobby.addField("Mains", StringUtil.getMainsFormatted(characters), true);
            }

            Random random = new Random();

            lobby.setFooter("ID: " + String.format("%03d", random.nextInt(9999 - 0 + 1) + 0));
        }
        e.getChannel().sendMessage(lobby.build()).queue(msg -> msg.addReaction(GlobalVar.CHECKMARK_EMOJI).queue());
    }
}
