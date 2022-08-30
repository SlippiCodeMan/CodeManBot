package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.FeedbackUtil;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;
import java.util.Objects;

public class AskCommand extends CodeManCommand {

    public AskCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        EmbedBuilder builder = new EmbedBuilder();
        Member author = Objects.requireNonNull(e.getMember());

        String code = DatabaseBridge.getCode(author.getIdLong());
        if (code == null) {
            e.reply(FeedbackUtil.NOTCONNECTED).setEphemeral(true).queue();
        } else {
            builder.setTitle("Netplay Search");
            builder.setDescription(author.getEffectiveName() + " is looking for an opponent!");
            builder.setThumbnail(author.getUser().getAvatarUrl());
            builder.setColor(GlobalVar.SUCCESS);

            builder.addField(StringUtil.getPersonPrefixedString(false, "code"), code, true);
            List<SSBMCharacter> characters = DatabaseBridge.getMains(e.getIdLong());
            if (characters != null && characters.size() != 0) {
                builder.addField(StringUtil.getPersonPrefixedString(false, "mains"), StringUtil.getMainsFormatted(characters), true);
            }

            builder.setFooter(GlobalVar.FROG_EMOJI + " slippi");
        }

        e.replyEmbeds(builder.build()).queue();
    }
}
