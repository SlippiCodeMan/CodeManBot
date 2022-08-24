package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class AskCommand extends CodeManCommand {

    public AskCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        /*
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

            builder.setFooter(GlobalVar.FROG_EMOJI + " slippi");
        }

        e.getChannel().sendMessage(builder.build()).queue();
         */
    }
}
