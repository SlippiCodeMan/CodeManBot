package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.Map;

public class MainCommand extends CodeManCommand {

    public MainCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e, Map<String, String> args) {
        if (DatabaseBridge.getCode(e.getAuthor().getIdLong()) == null) {
            e.getChannel().sendMessage(EmbedUtil.NOTCONNECTED.getEmbed().build()).queue();

            return;
        }

        String characterInput = args.get("char");
        EmbedBuilder builder = new EmbedBuilder();

        if (characterInput == null) {
            List<SSBMCharacter> result = DatabaseBridge.getMains(e.getAuthor().getIdLong());

            builder.setColor(GlobalVar.SUCCESS);
            String mains = StringUtil.getMainsFormatted(result);
            builder.addField(StringUtil.getPersonPrefixedString(true, "mains"), mains.isEmpty() ? StringUtil.italic("None") : mains, false);
        }
        else {
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
                DatabaseBridge.ToggleMainResult result = DatabaseBridge.toggleMain(e.getAuthor().getIdLong(), character);

                if (result.isAccepted()) {
                        builder.setColor(GlobalVar.SUCCESS);
                        builder.setDescription("Operation done!");
                        String oldMains = StringUtil.getMainsFormatted(result.getOldMains());
                        String newMains = StringUtil.getMainsFormatted(result.getNewMains());

                        builder.addField("Old mains", oldMains.isEmpty() ? StringUtil.italic("None") : oldMains, false);
                        builder.addField("New mains", newMains.isEmpty() ? StringUtil.italic("None") : newMains, false);
                    if (result.isAdding()) {
                        builder.setFooter(Application.EXEC_MODE.getCommandPrefix() + "main " + args.get("char") +  " to remove this main");
                    }

                } else {
                    builder.setColor(GlobalVar.ERROR);
                    builder.setDescription("Operation failed! You aren't allowed to have more than 3 mains!");
                    builder.setFooter(Application.EXEC_MODE.getCommandPrefix() + "main <character> to remove one of your mains");
                    builder.addField("Your mains", StringUtil.getMainsFormatted(result.getOldMains()), true);
                }
            } else {
                builder.setColor(GlobalVar.ERROR);
                builder.setDescription("Operation failed! Please write a valid character name!");
            }
        }
        e.getChannel().sendMessage(builder.build()).queue();
    }
}
