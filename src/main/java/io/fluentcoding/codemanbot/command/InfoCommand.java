package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommandWithArgs;
import io.fluentcoding.codemanbot.container.PagingContainer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfoCommand extends CodeManCommandWithArgs {

    public InfoCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e, Map<String, String> args) {
        String user = args.get("user");

        EmbedBuilder builder = new EmbedBuilder();

        boolean mentionedMemberIsAuthor = e.getMessage().getMentionedMembers().size() > 0 && e.getMessage().getMentionedMembers().get(0).getIdLong() == e.getAuthor().getIdLong();
        if (user == null || mentionedMemberIsAuthor) {
            String retrievedCode = DatabaseBridge.getCode(e.getAuthor().getIdLong());

            if (retrievedCode == null) {
                builder = EmbedUtil.notConnected(builder);
            } else {
                String mains = getMains(e.getAuthor().getIdLong());

                builder.setAuthor(GlobalVar.LOADING_EMOJI, null, e.getAuthor().getAvatarUrl());
                if (mains.isEmpty()) {
                    builder.setDescription(retrievedCode);
                } else {
                    builder.setDescription(retrievedCode + mains);
                }

                builder.setColor(GlobalVar.LOADING);
                e.getChannel().sendMessage(builder.build()).queue(msg -> {
                    EmbedBuilder newBuilder = new EmbedBuilder();

                    String name = SlippiBridge.getName(retrievedCode);
                    newBuilder.setAuthor(name == null ? e.getAuthor().getName() : name, null, e.getAuthor().getAvatarUrl());

                    if (mains.isEmpty()) {
                        newBuilder.setDescription(retrievedCode);
                    } else {
                        newBuilder.setDescription(retrievedCode + " " + mains);
                    }

                    newBuilder.setColor(GlobalVar.SUCCESS);
                    msg.editMessage(newBuilder.build()).queue();
                });

                return;
            }
        } else if (e.getMessage().getMentionedMembers().size() > 0) {
            Member mentionedMember = e.getMessage().getMentionedMembers().get(0);
            String retrievedCode = DatabaseBridge.getCode(mentionedMember.getIdLong());

            if (retrievedCode == null) {
                builder.setDescription("This person didn't connect to CodeMan yet!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                String mains = getMains(mentionedMember.getIdLong());

                builder.addField("Their code", retrievedCode, true);
                builder.addField("Their name", GlobalVar.LOADING_EMOJI, true);
                if (!mains.isEmpty()) {
                    builder.addField("Their mains", mains, true);
                }

                builder.setColor(GlobalVar.LOADING);
                e.getChannel().sendMessage(builder.build()).queue(msg -> {
                    EmbedBuilder newBuilder = new EmbedBuilder();
                    newBuilder.addField("Their code", retrievedCode, true);

                    String name = SlippiBridge.getName(retrievedCode);
                    newBuilder.addField("Their name", name, true);

                    if (!mains.isEmpty()) {
                        newBuilder.addField("Their mains", mains, true);
                    }

                    newBuilder.setColor(GlobalVar.SUCCESS);
                    msg.editMessage(newBuilder.build()).queue();
                });
                return;
            }
        } else if (PatternChecker.isConnectCode(user)) {
            long discordID = DatabaseBridge.getDiscordIdFromConnectCode(user.toUpperCase());
            builder.addField("Their name", GlobalVar.LOADING_EMOJI, true);
            String mains;
            if (discordID == -1) {
                mains = "";
            } else {
                mains = getMains(discordID);
                if (!mains.isEmpty()) {
                    builder.addField("Their mains", mains, true);
                }
            }
            builder.setColor(GlobalVar.LOADING);
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                EmbedBuilder newBuilder = new EmbedBuilder();
                String name = SlippiBridge.getName(user.toUpperCase());
                if (name == null) {
                    newBuilder.setDescription("This person doesn't exist!");
                    newBuilder.setColor(GlobalVar.ERROR);
                } else {
                    newBuilder.addField("Their name", name, true);
                    if (!mains.isEmpty()) {
                        newBuilder.addField("Their mains", mains, true);
                    }
                    newBuilder.setColor(GlobalVar.SUCCESS);
                }

                msg.editMessage(newBuilder.build()).queue();
            });
            return;
        } else if (PatternChecker.isSlippiUsername(user)) {
            builder.setTitle(GlobalVar.LOADING_EMOJI);
            builder.setColor(GlobalVar.LOADING);
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                List<SlippiBridge.UserEntry> codes = SlippiBridge.getCodesWithActualName(user);

                EmbedBuilder newBuilder = new EmbedBuilder();
                if (codes == null || codes.size() == 0) {
                    newBuilder.setDescription("This person doesn't exist!");
                    newBuilder.setColor(GlobalVar.ERROR);
                } else {
                    if (codes.size() == 1) {
                        SlippiBridge.UserEntry entry = codes.get(0);
                        long discordID = DatabaseBridge.getDiscordIdFromConnectCode(entry.getCode());
                        newBuilder.addField("Their code",
                                entry.getDisplayName() == null ? entry.getCode()
                                        : StringUtil.stringWithSlippiUsername(entry.getCode(), entry.getDisplayName()),
                                false);
                        String mains;
                        if (discordID == -1) {
                            mains = "";
                        } else {
                            mains = getMains(discordID);
                            if (!mains.isEmpty()) {
                                newBuilder.addField("Their mains", mains, true);
                            }
                        }
                        newBuilder.setColor(GlobalVar.SUCCESS);
                    } else {
                        Map<String, String> codesWithMains = new HashMap<>();
                        for (SlippiBridge.UserEntry entry : codes) {
                            long discordId = DatabaseBridge.getDiscordIdFromConnectCode(entry.getCode());
                            String mains = getMains(discordId);
                            if (!mains.isEmpty())
                                codesWithMains.put(entry.getCode(), mains);
                        }
                        
                        List<String> result = codes.stream()
                                .filter(entry -> entry.getDisplayName() == null && codesWithMains.containsKey(entry.getCode()))
                                .map(entry -> StringUtil.stringWithMains(
                                        entry.getCode(),
                                        codesWithMains.get(entry.getCode())
                                    )
                                )
                                .collect(Collectors.toList());
                        result.addAll(
                                codes.stream()
                                        .filter(entry -> entry.getDisplayName() == null && !codesWithMains.containsKey(entry.getCode()))
                                        .map(entry -> entry.getCode())
                                        .collect(Collectors.toList())
                        );
                        result.addAll(
                                codes.stream()
                                        .filter(entry -> entry.getDisplayName() != null && codesWithMains.containsKey(entry.getCode()))
                                        .map(entry -> StringUtil.stringWithSlippiUsernameAndMains(
                                                entry.getCode(),
                                                entry.getDisplayName(),
                                                codesWithMains.get(entry.getCode())
                                            )
                                        )
                                        .collect(Collectors.toList())
                        );
                        result.addAll(
                                codes.stream()
                                        .filter(entry -> entry.getDisplayName() != null && !codesWithMains.containsKey(entry.getCode()))
                                        .map(entry -> StringUtil.stringWithSlippiUsername(
                                                entry.getCode(),
                                                entry.getDisplayName()
                                            )
                                        )
                                        .collect(Collectors.toList())
                        );

                        String title = StringUtil.bold(codes.size() + " players are using this username:\n\n");

                        if (result.size() > GlobalVar.MAX_ITEMS_PER_PAGE) {
                            PagingContainer.INSTANCE.pageableMessageHandler(msg::editMessage,
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

    private String getMains(long discordId) {
        return StringUtil.getMainsFormatted(DatabaseBridge.getMains(discordId));
    }
}
