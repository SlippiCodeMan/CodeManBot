package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommandWithArgs;
import io.fluentcoding.codemanbot.container.PagingContainer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class WhoisCommand extends CodeManCommandWithArgs {

    public WhoisCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(MessageReceivedEvent e, Map<String, String> args) {
        String user = args.get("user");

        EmbedBuilder builder = new EmbedBuilder();

        if (PatternChecker.isConnectCode(user)) {
            final String code = user.toUpperCase();
            long discordId = DatabaseBridge.getDiscordIdFromConnectCode(code);

            // ERROR
            if (discordId == -1L) {
                builder.setDescription("*Loading...*");
                builder.setColor(GlobalVar.ERROR);

                e.getChannel().sendMessage(builder.build()).queue(msg -> {
                    if (!SlippiBridge.userWithCodeExists(code)) {
                        builder.setDescription("This connect code has no discord user associated to it!");
                    } else {
                        builder.setDescription("Nobody uses this username!");
                    }

                    msg.editMessage(builder.build()).queue();
                });

                return;
            } else {
                User discordUser = e.getJDA().retrieveUserById(discordId).complete();
                builder.addField("Their discord tag", discordUser.getAsTag(), false);
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else if (PatternChecker.isSlippiUsername(user)) {
            builder.addField("Their discord tag", "*Loading...*", false);
            builder.setColor(GlobalVar.LOADING);

            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                List<SlippiBridge.UserEntry> codes = SlippiBridge.getCodesWithActualName(user);
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
                                    "Their discord tag",
                                    entry.getDisplayName() == null ? discordUser.getAsTag() : StringUtil.stringWithSlippiUsername(discordUser.getAsTag(), entry.getDisplayName()),
                                    false
                            );
                            newBuilder.setColor(GlobalVar.SUCCESS);
                            msg.editMessage(newBuilder.build()).queue();
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

                        String title = "**" + result.size() + " players are using this username:**\n\n";

                        if (result.size() > GlobalVar.MAX_ITEMS_PER_PAGE) {
                            PagingContainer.INSTANCE.pageableMessageHandler(e.getChannel()::sendMessage,
                                    new PagingContainer.PageableContent(title, result.stream().toArray(String[]::new), e.getAuthor().getIdLong()));
                            return;
                        } else {
                            String content = String.join("\n", result);
                            newBuilder.setDescription(title + content);
                            newBuilder.setColor(GlobalVar.SUCCESS);
                        }
                    }
                }

                msg.editMessage(newBuilder.build()).queue();
            });

            return;
        } else {
            builder.setDescription("This parameter could neither get recognized as an username nor as a connect code!");
            builder.setColor(GlobalVar.ERROR);
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }

    @AllArgsConstructor
    @Getter
    static class UserDiscordEntry {
        private String displayName;
        private long discordId;
    }
}
