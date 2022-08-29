package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Objects;

public class ColorCommand extends CodeManCommand {

    public ColorCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        long authorId = Objects.requireNonNull(e.getMember()).getIdLong();

        if (DatabaseBridge.getCode(authorId) == null) {
            e.reply(FeedbackUtil.NOTCONNECTED).setEphemeral(true).queue();
            return;
        }

        String colorInput = Objects.requireNonNull(e.getOption("color")).getAsString();
        EmbedBuilder builder = new EmbedBuilder();

        // boolean allLetters = colorInput.chars().allMatch(Character::isLetter);
        int resultColor = -1;

        if (PatternChecker.isHexColorPattern(colorInput)) {
            resultColor = Integer.parseInt(colorInput.substring(1), 16);
        } else {
            String name = colorInput.replaceAll("\\s+","").toLowerCase();
            resultColor = ColorUtil.getColorFromName(name);
        }

        if (resultColor != -1) {
            boolean result = DatabaseBridge.insertColor(authorId, resultColor);

            if (result) {
                String suffix = "";
                String colorName = ColorUtil.getNameFromColor(resultColor);
                if (colorName != null) {
                    suffix = StringUtil.bold(" (" + colorName + ")");
                }

                builder.setColor(resultColor);
                builder.setDescription("Operation done!");
                builder.addField("New color", "#" + Integer.toHexString(resultColor).toUpperCase() + suffix, false);
                builder.setFooter("Try out /info to see your new color!");
            } else {
                builder.setColor(GlobalVar.ERROR);
                builder.setDescription("Operation failed! Internal error.");
            }
        } else {
            builder.setColor(GlobalVar.ERROR);
            builder.setDescription("Operation failed! Please write a valid color (either a known color name or a hex color that starts with a #)!");
            builder.setFooter("Example: /color color:#ffffff");
        }
        e.replyEmbeds(builder.build()).queue();
    }
}
