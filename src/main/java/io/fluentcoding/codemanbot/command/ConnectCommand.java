package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommandWithArgs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Map;

public class ConnectCommand extends CodeManCommandWithArgs {

    public ConnectCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e, Map<String, String> args) {
        String code = args.get("code").toUpperCase();
        boolean isValid = PatternChecker.isConnectCode(code);

        EmbedBuilder builder = new EmbedBuilder();
        if (isValid) {
            long discordId = DatabaseBridge.getDiscordIdFromConnectCode(code);
            if (e.getAuthor().getIdLong() == discordId) {
                builder.setDescription("You are already connected to this code!");
                builder.setColor(GlobalVar.ERROR);
                e.getChannel().sendMessage(builder.build()).queue();
                return;
            }

            builder.setTitle(GlobalVar.LOADING_EMOJI);
            builder.setColor(GlobalVar.LOADING);
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                EmbedBuilder newBuilder = new EmbedBuilder();
                if (!SlippiBridge.userWithCodeExists(code)) {
                    newBuilder.setColor(GlobalVar.ERROR);
                    newBuilder.setDescription("This connect code doesn't exist!");
                } else {
                    DatabaseBridge.InsertCodeResult result = DatabaseBridge.insertCode(e.getAuthor().getIdLong(), code);

                    if (result.isAccepted()) {
                        newBuilder.setColor(GlobalVar.SUCCESS);
                        newBuilder.setDescription("Operation done!");
                        if (result.getOldCode() != null) {
                            newBuilder.addField("Old Code", result.getOldCode(), true);
                        }
                        newBuilder.addField("New Code", code, true);
                        ActivityUpdater.update(e.getJDA());
                    } else {
                        newBuilder.setColor(GlobalVar.ERROR);
                        newBuilder.setDescription("Operation failed! Someone already uses this code!\nContact **Ananas#5903** or **FluentCoding#3314**!");
                    }
                }

                msg.editMessage(newBuilder.build()).queue();
            });

            return;
        } else {
            builder.setColor(GlobalVar.ERROR);
            builder.setDescription("Operation failed! Your tag format should be like this:\n**ABCD#123**");
            e.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
