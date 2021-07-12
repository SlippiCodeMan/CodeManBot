package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBotBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.container.ConnectContainer;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConnectCommand extends CodeManCommand {

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
                builder = EmbedUtil.ALREADYCONNECTED.getEmbed();
                e.getChannel().sendMessage(builder.build()).queue();
                return;
            }

            final ConnectContainer.ConnectInformationKey information = new ConnectContainer.ConnectInformationKey(code, e.getAuthor().getIdLong());
            if (ConnectContainer.INSTANCE.isConnecting(information)) {
                builder = EmbedUtil.ISCONNECTING.getEmbed();
                e.getChannel().sendMessage(builder.build()).queue();
            }

            ConnectContainer.INSTANCE.addConnectInformation(information);

            builder.setTitle(GlobalVar.LOADING_EMOJI);
            builder.setColor(GlobalVar.LOADING);
            Future<Boolean> userWithCodeExistsFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.userWithCodeExists(code));
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                boolean userWithCodeExists;
                try {
                    userWithCodeExists = userWithCodeExistsFuture.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    userWithCodeExists = false;
                }
                EmbedBuilder newBuilder = new EmbedBuilder();
                if (!userWithCodeExists) {
                    newBuilder.setColor(GlobalVar.ERROR);
                    newBuilder.setDescription("This connect code doesn't exist!");
                } else {
                    boolean codeAlreadyTaken = DatabaseBridge.codeAlreadyTaken(code);

                    if (!codeAlreadyTaken) {
                        newBuilder.setDescription("We've sent you the instructions via DM on how to connect your account!");
                        newBuilder.setColor(GlobalVar.SUCCESS);

                        e.getAuthor().openPrivateChannel().queue(privateChannel -> {
                            ConnectContainer.INSTANCE.setPrivateChannel(information, privateChannel);
                            try {
                                SlippiBotBridge.sendQueue(information);
                            } catch (JSONException | IOException jsonException) {
                                jsonException.printStackTrace();
                                ConnectContainer.INSTANCE.removeConnectInformation(information);
                            }
                        });
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
