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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ColorCommand extends CodeManCommand {

    public ColorCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e, Map<String, String> args) {
        if (DatabaseBridge.getCode(e.getAuthor().getIdLong()) == null) {
            e.getChannel().sendMessage(EmbedUtil.NOTCONNECTED.getEmbed().build()).queue();

            return;
        }

        String colorInput = args.get("char");
        EmbedBuilder builder = new EmbedBuilder();

        if (colorInput == null) {
            int color = DatabaseBridge.getColor(e.getAuthor().getIdLong());

            builder.setColor(color);
            builder.addField(StringUtil.getPersonPrefixedString(true, "color"), "#" + Integer.toHexString(color).toUpperCase(), false);
        }
        else {
            // boolean allLetters = colorInput.chars().allMatch(Character::isLetter);
            int resultColor = -1;

            if (PatternChecker.isHexColorPattern(colorInput)) {
                resultColor = Integer.parseInt(colorInput.substring(1), 16);
            } else {
                // doing it later
                resultColor = GlobalVar.SUCCESS.getRGB();
            }

            if (resultColor != -1) {
                boolean result = DatabaseBridge.insertColor(e.getAuthor().getIdLong(), resultColor);

                if (result) {
                    builder.setColor(resultColor);
                    builder.setDescription("Operation done!");
                    builder.setFooter("Try out " + Application.EXEC_MODE.getCommandPrefix() + "info to see your new color!");
                } else {
                    builder.setColor(GlobalVar.ERROR);
                    builder.setDescription("Operation failed! Internal error.");
                }
            } else {
                builder.setColor(GlobalVar.ERROR);
                builder.setDescription("Operation failed! Please write a valid color!");
            }
        }
        e.getChannel().sendMessage(builder.build()).queue();
    }
}
