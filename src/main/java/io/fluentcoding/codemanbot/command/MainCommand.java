package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.FeedbackUtil;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Objects;

public class MainCommand extends CodeManCommand {

    public MainCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        long authorId = Objects.requireNonNull(e.getMember()).getIdLong();
        if (DatabaseBridge.getCode(authorId) == null) {
            e.reply(FeedbackUtil.NOTCONNECTED).setEphemeral(true).queue();
            return;
        }

        String characterInput = Objects.requireNonNull(e.getOption("character")).getAsString();

        EmbedBuilder builder = new EmbedBuilder();

        characterInput = characterInput.replaceAll("[\\s-'.&]", "").toLowerCase();
        SSBMCharacter character = null;

        outer:
        for (SSBMCharacter tempChar : SSBMCharacter.values()) {

            for (String identifier : tempChar.getIdentifiers()) {
                if (identifier.equals(characterInput)) {
                    character = tempChar;
                    break outer;
                }
            }
        }

        if (character != null) {
            DatabaseBridge.ToggleMainResult result = DatabaseBridge.toggleMain(authorId, character);

            if (result.isAccepted()) {
                    builder.setColor(GlobalVar.SUCCESS);
                    builder.setDescription("Operation done!");
                    String oldMains = StringUtil.getMainsFormatted(result.getOldMains());
                    String newMains = StringUtil.getMainsFormatted(result.getNewMains());

                    builder.addField("Old mains", oldMains.isEmpty() ? StringUtil.italic("None") : oldMains, false);
                    builder.addField("New mains", newMains.isEmpty() ? StringUtil.italic("None") : newMains, false);
                if (result.isAdding()) {
                    builder.setFooter("/main " + characterInput + " to remove this main");
                }

            } else {
                builder.setColor(GlobalVar.ERROR);
                builder.setDescription("Operation failed! You aren't allowed to have more than 3 mains!");
                builder.setFooter("/main <character> to remove one of your mains");
                builder.addField("Your mains", StringUtil.getMainsFormatted(result.getOldMains()), true);
            }

            e.replyEmbeds(builder.build()).queue();

        } else {
            e.reply("Invalid character name!").setEphemeral(true).queue();
        }
    }
}
