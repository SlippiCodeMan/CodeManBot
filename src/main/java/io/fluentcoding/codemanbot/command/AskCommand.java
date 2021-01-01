package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.EmbedUtil;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.Map;

public class AskCommand extends CodeManCommand {

    public AskCommand(String description, String prefix, String... aliases) {
        super(description, prefix, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e, Map args) {
        EmbedBuilder builder = new EmbedBuilder();

        String code = DatabaseBridge.getCode(e.getAuthor().getIdLong());
        if (code == null) {
            builder = EmbedUtil.NOTCONNECTED.getEmbed();
        } else {
            builder.setTitle("Netplay Search");
            builder.setDescription(e.getAuthor().getName() + " is looking for an opponent!");
            builder.setThumbnail(e.getAuthor().getAvatarUrl());
            builder.setColor(GlobalVar.SUCCESS);

            builder.addField(StringUtil.getPersonPrefixedString(false, "code"), code, true);
            List<SSBMCharacter> characters = DatabaseBridge.getMains(e.getAuthor().getIdLong());
            if (characters != null && characters.size() != 0) {
                builder.addField(StringUtil.getPersonPrefixedString(false, "mains"), StringUtil.getMainsFormatted(characters), true);
            }

            builder.setFooter(GlobalVar.FROG_EMOJI + " slippi 2.x.x");
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
