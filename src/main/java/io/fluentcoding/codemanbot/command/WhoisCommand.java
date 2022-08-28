package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.container.PagingContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class WhoisCommand extends CodeManCommand {

    public WhoisCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        if (e.getOptions().size() != 1) {
            e.reply("You must provide exactly one argument!")
                    .setEphemeral(true)
                    .queue();
        } else if (e.getOption("username") != null) {
            String user = Objects.requireNonNull(e.getOption("user")).getAsString();

            Future<List<SlippiBridge.UserEntry>> codesFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.getCodesWithActualName(user));
            e.deferReply().queue(interactionHook -> {
                List<SlippiBridge.UserEntry> codes;
                try {
                    codes = codesFuture.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    codes = null;
                }
                EmbedBuilder newBuilder = new EmbedBuilder();

                // ERROR
                if (codes == null || codes.size() == 0) {
                    newBuilder.setDescription("Nobody uses this username!");
                    newBuilder.setColor(GlobalVar.ERROR);
                } else {
                    List<UserDiscordEntry> userEntries = new ArrayList<>();
                    for (SlippiBridge.UserEntry entry : codes) {
                        long discordId = DatabaseBridge.getDiscordIdFromConnectCode(entry.getCode().toUpperCase());
                        if (discordId != -1)
                            userEntries.add(new UserDiscordEntry(entry.getDisplayName(), discordId));
                    }

                    if (userEntries.size() == 0) {
                        newBuilder.setDescription("This username has no discord users associated to it!");
                        newBuilder.setColor(GlobalVar.ERROR);
                    } else if (userEntries.size() == 1) {
                        UserDiscordEntry entry = userEntries.get(0);
                        e.getJDA().retrieveUserById(entry.getDiscordId()).queue(discordUser -> {
                            newBuilder.addField(
                                    StringUtil.getPersonPrefixedString(false, "discord tag"),
                                    entry.getDisplayName() == null ? discordUser.getAsTag() : StringUtil.stringWithSlippiUsername(discordUser.getAsTag(), entry.getDisplayName()),
                                    false
                            );
                            newBuilder.setColor(GlobalVar.SUCCESS);
                            e.getHook().sendMessageEmbeds(newBuilder.build()).queue();
                        });

                        return;
                    } else {
                        List<String> result = userEntries.stream()
                                .filter(entry -> entry.getDisplayName() == null)
                                .map(entry -> {
                                    try {
                                        return e.getJDA().retrieveUserById(entry.getDiscordId()).submit().get().getAsTag();
                                    } catch (InterruptedException interruptedException) {
                                        interruptedException.printStackTrace();
                                    } catch (ExecutionException executionException) {
                                        executionException.printStackTrace();
                                    }
                                    return null;
                                })
                                .collect(Collectors.toList());
                        result.addAll(userEntries.stream()
                                .filter(entry -> entry.getDisplayName() != null)
                                .map(entry -> {
                                    try {
                                        return StringUtil.stringWithSlippiUsername(
                                                e.getJDA().retrieveUserById(entry.getDiscordId()).submit().get().getAsTag(),
                                                entry.getDisplayName());
                                    } catch (InterruptedException interruptedException) {
                                        interruptedException.printStackTrace();
                                    } catch (ExecutionException executionException) {
                                        executionException.printStackTrace();
                                    }
                                    return null;
                                })
                                .collect(Collectors.toList()));

                        String title = StringUtil.bold(result.size() + " players are using this username:\n\n");

                        if (result.size() > GlobalVar.MAX_ITEMS_PER_PAGE) {
                            PagingContainer.INSTANCE.pageableMessageHandler(e.getChannel()::sendMessage,
                                    new PagingContainer.PageableContent(title, result.stream().toArray(String[]::new), e.getUser().getIdLong()));
                            return;
                        } else {
                            String content = String.join("\n", result);
                            newBuilder.setDescription(title + content);
                            newBuilder.setColor(GlobalVar.SUCCESS);
                        }
                    }
                }

                e.getHook().sendMessageEmbeds(newBuilder.build()).queue();
            });

            return;
        } else if (e.getOption("code") != null) {
            String code = Objects.requireNonNull(e.getOption("code")).getAsString().toUpperCase();
            long discordId = DatabaseBridge.getDiscordIdFromConnectCode(code);

            EmbedBuilder builder = new EmbedBuilder();

            // ERROR
            if (discordId == -1L) {
                Future<Boolean> userWithCodeExistsFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.userWithCodeExists(code));
                e.deferReply().queue(interactionHook -> {
                    boolean userWithCodeExists;
                    try {
                        userWithCodeExists = userWithCodeExistsFuture.get();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        userWithCodeExists = false;
                    }
                    builder.setColor(GlobalVar.ERROR);
                    if (!userWithCodeExists) {
                        builder.setDescription("Nobody uses this connect code!");
                    } else {
                        builder.setDescription("This connect code has no discord user associated to it!");
                    }
                    e.getHook().sendMessageEmbeds(builder.build()).queue();
                });

                return;
            } else {
                User discordUser = e.getJDA().retrieveUserById(discordId).complete();
                builder.addField(StringUtil.getPersonPrefixedString(false, "discord tag"), discordUser.getAsTag(), false);
                builder.setColor(GlobalVar.SUCCESS);

                e.replyEmbeds(builder.build()).queue();
            }
        }
    }

    @AllArgsConstructor
    @Getter
    static class UserDiscordEntry {
        private String displayName;
        private long discordId;
    }
}
