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

        if (isValid) {
            long discordId = DatabaseBridge.getDiscordIdFromConnectCode(code);
            if (authorId == discordId) {
                e.reply(FeedbackUtil.ALREADYCONNECTED).setEphemeral(true).queue();
                return;
            }

            final ConnectContainer.ConnectInformationKey information = new ConnectContainer.ConnectInformationKey(code, e.getMember().getIdLong());
            if (ConnectContainer.INSTANCE.isConnecting(authorId)) {
                e.reply(FeedbackUtil.ISCONNECTING).setEphemeral(true).queue();
                return;
            }

            ConnectContainer.INSTANCE.addConnectInformation(information);

            Future<Boolean> userWithCodeExistsFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.userWithCodeExists(code));
            e.deferReply().queue(interactionHook -> {
                ConnectContainer.INSTANCE.removeConnectInformation(information);
                boolean userWithCodeExists;
                try {
                    userWithCodeExists = userWithCodeExistsFuture.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    userWithCodeExists = false;
                }
                EmbedBuilder builder = new EmbedBuilder();

                if (!userWithCodeExists) {
                    builder.setColor(GlobalVar.ERROR);
                    builder.setDescription("It seems this connect code doesn't exist!");
                    ConnectContainer.INSTANCE.removeConnectInformation(information);
                } else {
                    boolean codeAlreadyTaken = DatabaseBridge.codeAlreadyTaken(code);

                    if (!codeAlreadyTaken) {
                        if (SlippiBotBridge.isConnected()) {
                            builder.setDescription("We've sent you the instructions via DM on how to connect your account!");
                            builder.setColor(GlobalVar.SUCCESS);

                            e.getJDA().openPrivateChannelById(e.getMember().getIdLong()).queue(privateChannel -> {
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
                            builder.setDescription("Success!");
                            builder.setColor(GlobalVar.SUCCESS);
                        } else {
                            builder.setDescription("We weren't able to connect to our bot service!");
                            builder.setColor(GlobalVar.ERROR);
                            ConnectContainer.INSTANCE.removeConnectInformation(information);
                        }
                    } else {
                        builder.setColor(GlobalVar.ERROR);
                        builder.setDescription("Operation failed! Someone already uses this code!\nContact **Ananas#5903** or **FluentCoding#3314**!");
                        ConnectContainer.INSTANCE.removeConnectInformation(information);
                    }
                }

                e.getHook().sendMessageEmbeds(builder.build()).queue();
            });

        } else {
            e.reply("Syntax Error! Your code format should be like this:\n**ABCD#123**")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
