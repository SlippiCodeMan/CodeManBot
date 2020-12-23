package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommandWithArgs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

public class ConnectCommand extends CodeManCommandWithArgs {

    public ConnectCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(MessageReceivedEvent e, Map<String, String> args) {
        String code = args.get("code");
        boolean isValid = PatternChecker.isConnectCode(code);

        EmbedBuilder builder = new EmbedBuilder();
        if (isValid) {
            code = code.toUpperCase();
            DatabaseBridge.InsertCodeResult result = DatabaseBridge.insertCode(e.getAuthor().getIdLong(), code);

            if (result.isAccepted()) {
                builder.setColor(GlobalVar.SUCCESS);
                builder.setDescription("Operation done!");
                if (result.getOldCode() != null) {
                    builder.addField("Old Code", result.getOldCode(), true);
                }
                builder.addField("New Code", code, true);
                ActivityUpdater.update(e.getJDA());
            } else {
                builder.setColor(GlobalVar.ERROR);
                builder.setDescription("Operation failed! Someone already uses this code!\nContact **Ananas#5903** or **FluentCoding#3314**!");
            }
        } else {
            builder.setColor(GlobalVar.ERROR);
            builder.setDescription("Operation failed! Your tag format should be like this:\n**ABCD#123**");
        }
        e.getChannel().sendMessage(builder.build()).queue();
    }
}
