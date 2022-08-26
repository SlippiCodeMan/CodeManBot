package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBotBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.container.ConnectContainer;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.json.JSONException;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConnectCommand extends CodeManCommand {

    public ConnectCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        String code = e.getOption("code").getAsString().toUpperCase();
        long authorId = e.getMember().getIdLong();
        boolean isValid = PatternChecker.isConnectCode(code);

        EmbedBuilder builder = new EmbedBuilder();
        if (isValid) {
            long discordId = DatabaseBridge.getDiscordIdFromConnectCode(code);
            if (authorId == discordId) {
                e.reply(FeedbackUtil.ALREADYCONNECTED).setEphemeral(true).queue();
                return;
            }

            final ConnectContainer.ConnectInformationKey information = new ConnectContainer.ConnectInformationKey(code, e.getAuthor().getIdLong());
            if (ConnectContainer.INSTANCE.isConnecting(authorId)) {
                e.reply(FeedbackUtil.ISCONNECTING).setEphemeral(true).queue();
                return;
            }

            ConnectContainer.INSTANCE.addConnectInformation(information);

            builder.setTitle(GlobalVar.LOADING_EMOJI);
            builder.setColor(GlobalVar.LOADING);
            Future<Boolean> userWithCodeExistsFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.userWithCodeExists(code));
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                ConnectContainer.INSTANCE.removeConnectInformation(information);
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
                    ConnectContainer.INSTANCE.removeConnectInformation(information);
                } else {
                    boolean codeAlreadyTaken = DatabaseBridge.codeAlreadyTaken(code);

                    if (!codeAlreadyTaken) {
                        if (SlippiBotBridge.isConnected()) {
                            newBuilder.setDescription("We've sent you the instructions via DM on how to connect your account!");
                            newBuilder.setColor(GlobalVar.SUCCESS);

                            e.getMember().openPrivateChannel().queue(privateChannel -> {
                                ConnectContainer.INSTANCE.setPrivateChannel(information, privateChannel);
                                try {
                                    SlippiBotBridge.sendQueue(information);
                                } catch (JSONException | IOException jsonException) {
                                    jsonException.printStackTrace();
                                    ConnectContainer.INSTANCE.removeConnectInformation(information);
                                }
                            });
                        } else if (GlobalVar.dotenv.get("CODEMAN_EXEC_MODE").equals("dev")) {
                            ConnectContainer.INSTANCE.removeConnectInformation(information);
                            DatabaseBridge.insertCode(information.getUserId(), information.getCode());
                            newBuilder.setDescription("Success!");
                            newBuilder.setColor(GlobalVar.SUCCESS);
                        } else {
                            newBuilder.setDescription("We weren't able to connect to our bot service!");
                            newBuilder.setColor(GlobalVar.ERROR);
                            ConnectContainer.INSTANCE.removeConnectInformation(information);
                        }
                    } else {
                        newBuilder.setColor(GlobalVar.ERROR);
                        newBuilder.setDescription("Operation failed! Someone already uses this code!\nContact **Ananas#5903** or **FluentCoding#3314**!");
                        ConnectContainer.INSTANCE.removeConnectInformation(information);
                    }
                }

                msg.editMessage(newBuilder.build()).queue();
            });

        } else {
            builder.setColor(GlobalVar.ERROR);
            builder.setDescription("Operation failed! Your tag format should be like this:\n**ABCD#123**");
            e.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
