package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.ActivityUpdater;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.PatternChecker;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommandWithArgs;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;
import java.util.stream.Collectors;

public class MainCommand extends CodeManCommandWithArgs {

    public MainCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(MessageReceivedEvent e, Map<String, String> args) {
        String characterInput = args.get("char").replaceAll("[\\s-'.&]", "").toLowerCase();
        SSBMCharacter character = null;

        outer: for (SSBMCharacter tempChar : SSBMCharacter.values()) {
            for (String identifier : tempChar.getIdentifiers()) {
                if (identifier.equals(characterInput)) {
                    character = tempChar;
                    break outer;
                }
            }
        }

        EmbedBuilder builder = new EmbedBuilder();
        if (character != null) {
            DatabaseBridge.ToggleMainResult result = DatabaseBridge.toggleMain(e.getAuthor().getIdLong(), character);

            if (result.isAccepted()) {
                builder.setColor(GlobalVar.SUCCESS);
                builder.setDescription("Operation done!");
                builder.addField("Old mains", result.getOldMains().stream().map(main -> main.getName()).collect(Collectors.joining(", ")), true);
                builder.addField("New mains", result.getNewMains().stream().map(main -> main.getName()).collect(Collectors.joining(", ")), true);
            } else {
                builder.setColor(GlobalVar.ERROR);
                builder.setDescription("Operation failed! You aren't allowed to have more than 3 mains!");
                builder.addField("Your mains", result.getOldMains().stream().map(main -> main.getName()).collect(Collectors.joining(", ")), true);
            }
        } else {
            builder.setColor(GlobalVar.ERROR);
            builder.setDescription("Operation failed! Please write a valid character name!");
        }
        e.getChannel().sendMessage(builder.build()).queue();
    }
}
