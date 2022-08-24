package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.container.PagingContainer;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ColorCommand extends CodeManCommand {

    public ColorCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        /*
        if (DatabaseBridge.getCode(e.getAuthor().getIdLong()) == null) {
            e.getChannel().sendMessage(EmbedUtil.NOTCONNECTED.getEmbed().build()).queue();

            return;
        }

        String colorInput = args.get("color");
        EmbedBuilder builder = new EmbedBuilder();

        if (colorInput == null) {
            int color = DatabaseBridge.getColor(e.getAuthor().getIdLong());

            String suffix = "";
            String colorName = ColorUtil.getNameFromColor(color);
            if (colorName != null) {
                suffix = StringUtil.bold(" (" + colorName + ")");
            }

            builder.setColor(color);
            builder.addField(StringUtil.getPersonPrefixedString(true, "color"), "#" + Integer.toHexString(color).toUpperCase() + suffix, false);
        }
        else {
            // boolean allLetters = colorInput.chars().allMatch(Character::isLetter);
            int resultColor = -1;

            if (PatternChecker.isHexColorPattern(colorInput)) {
                resultColor = Integer.parseInt(colorInput.substring(1), 16);
            } else {
                String name = colorInput.replaceAll("\\s+","").toLowerCase();
                resultColor = ColorUtil.getColorFromName(name);
            }

            if (resultColor != -1) {
                boolean result = DatabaseBridge.insertColor(e.getAuthor().getIdLong(), resultColor);

                if (result) {
                    String suffix = "";
                    String colorName = ColorUtil.getNameFromColor(resultColor);
                    if (colorName != null) {
                        suffix = StringUtil.bold(" (" + colorName + ")");
                    }

                    builder.setColor(resultColor);
                    builder.setDescription("Operation done!");
                    builder.addField("New color", "#" + Integer.toHexString(resultColor).toUpperCase() + suffix, false);
                    builder.setFooter("Try out " + Application.EXEC_MODE.getCommandPrefix() + "info to see your new color!");
                } else {
                    builder.setColor(GlobalVar.ERROR);
                    builder.setDescription("Operation failed! Internal error.");
                }
            } else {
                builder.setColor(GlobalVar.ERROR);
                builder.setDescription("Operation failed! Please write a valid color (either a known color name or a hex color that starts with a #)!");
                builder.setFooter("Example: " + Application.EXEC_MODE.getCommandPrefix() + "color #ffffff");
            }
        }
        e.getChannel().sendMessage(builder.build()).queue();

         */
    }
}
