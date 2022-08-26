package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.container.PagingContainer;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InfoCommand extends CodeManCommand {

    public InfoCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        if (e.getOptions().size() > 1) {
            e.reply("You must not provide more than one argument!")
                    .setEphemeral(true)
                    .queue();
        } else if (e.getOptions().size() == 0) {
            Member member = e.getMember();
            String retrievedCode = DatabaseBridge.getCode(member.getIdLong());

            if (retrievedCode == null) {
                e.reply(FeedbackUtil.NOTCONNECTED).setEphemeral(true).queue();
            } else {
                outputWithDiscordId(member.getIdLong(), retrievedCode, e, true);
            }
        } else if (e.getOption("discord") != null) {
            Member member = e.getOption("discord").getAsMember();
            String retrievedCode = DatabaseBridge.getCode(member.getIdLong());

            if (retrievedCode == null) {
                if (member.getIdLong() == e.getMember().getIdLong()) {
                    e.reply(FeedbackUtil.NOTCONNECTED).setEphemeral(true).queue();
                } else {
                    e.reply("This person didn't connect to CodeMan yet!").setEphemeral(true).queue();
                }
            } else {
                outputWithDiscordId(member.getIdLong(), retrievedCode, e, true);
            }
        } else if (e.getOption("username") != null) {
            String username = Objects.requireNonNull(e.getOption("username")).getAsString();
            if (PatternChecker.isSlippiUsername(username)) {
                Future<List<SlippiBridge.UserEntry>> codesFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.getCodesWithActualName(username));
                e.deferReply().queue(interactionHook -> {
                    List<SlippiBridge.UserEntry> codes;
                    try {
                        codes = codesFuture.get(5, TimeUnit.SECONDS);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        codes = null;
                    }

                    EmbedBuilder builder = new EmbedBuilder();

                    if (codes == null || codes.size() == 0) {
                        builder.setColor(GlobalVar.ERROR);
                        builder.setDescription(StringUtil.bold(username) + " does not exist!");
                    } else if (codes.size() == 1) {
                        SlippiBridge.UserEntry entry = codes.get(0);
                        long discordID = DatabaseBridge.getDiscordIdFromConnectCode(entry.getCode());
                        builder.addField(StringUtil.getPersonPrefixedString(false, "code"),
                                entry.getDisplayName() == null ? entry.getCode()
                                        : StringUtil.stringWithSlippiUsername(entry.getCode(), entry.getDisplayName()),
                                true);
                        if (discordID != -1) {
                            String mains = getMains(discordID);
                            if (!mains.isEmpty()) {
                                builder.addField(StringUtil.getPersonPrefixedString(false, "mains"), mains, true);
                            }
                        }
                        builder.setColor(DatabaseBridge.getColor(discordID));
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

                        String title = StringUtil.bold( codes.size() + " players are using this username:") + "\n\n";

                        if (result.size() > GlobalVar.MAX_ITEMS_PER_PAGE) {
                            PagingContainer.INSTANCE.pageableMessageHandler(e.getHook()::editOriginalEmbeds,
                                    new PagingContainer.PageableContent(title, result.stream().toArray(String[]::new), e.getMember().getIdLong()));
                            return;
                        } else {
                            builder.setColor(GlobalVar.SUCCESS);
                            String content = String.join("\n", result);
                            builder.setDescription(title + content);
                        }
                        e.getHook().sendMessageEmbeds(builder.build()).queue();
                    }
                });
            } else {
                e.reply("Invalid username!").setEphemeral(true).queue();
            }
        } else if (e.getOption("code") != null) {
            String code = Objects.requireNonNull(e.getOption("code")).getAsString().toUpperCase();
            System.out.println(code);
            if (PatternChecker.isConnectCode(code)) {
                long discordID = DatabaseBridge.getDiscordIdFromConnectCode(code);
                if (discordID == -1) {
                    // TODO: show code and username
                    e.reply("This person didn't connect to CodeMan yet!").setEphemeral(true).queue();
                } else {
                    boolean you = (discordID == e.getMember().getIdLong());
                    outputWithDiscordId(discordID, code, e, you);
                }
            } else {
                e.reply("Invalid connect code!").setEphemeral(true).queue();
            }
        }

        /*

        boolean mentionedMemberIsAuthor = e.getMessage().getMentionedMembers().size() > 0 && e.getMessage().getMentionedMembers().get(0).getIdLong() == e.getAuthor().getIdLong();
        if (user == null || mentionedMemberIsAuthor) {
            String retrievedCode = DatabaseBridge.getCode(e.getAuthor().getIdLong());

            if (retrievedCode == null) {
                builder = EmbedUtil.NOTCONNECTED.getEmbed();
            } else {
                output(e.getAuthor().getIdLong(), retrievedCode, e, true);
                return;
            }
        } else if (e.getMessage().getMentionedMembers().size() > 0) {
            Member mentionedMember = e.getMessage().getMentionedMembers().get(0);
            String retrievedCode = DatabaseBridge.getCode(mentionedMember.getIdLong());

            if (retrievedCode == null) {
                builder.setDescription("This person didn't connect to CodeMan yet!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                output(mentionedMember.getIdLong(), retrievedCode, e, false);
                return;
            }
        } else if (PatternChecker.isConnectCode(user)) {
            long discordID = DatabaseBridge.getDiscordIdFromConnectCode(user.toUpperCase());
            builder.addField(StringUtil.getPersonPrefixedString(false, "name"), GlobalVar.LOADING_EMOJI, true);
            String mains;
            if (discordID == -1) {
                mains = "";
            } else {
                mains = getMains(discordID);
                if (!mains.isEmpty()) {
                    builder.addField(StringUtil.getPersonPrefixedString(false, "mains"), mains, true);
                }
            }
            builder.setColor(GlobalVar.LOADING);
            Future<String> nameFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.getName(user.toUpperCase()));
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                EmbedBuilder newBuilder = new EmbedBuilder();
                String name;
                try {
                    name = nameFuture.get(5, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    name = null;
                }
                if (name == null) {
                    newBuilder.setDescription("This person doesn't exist!");
                    newBuilder.setColor(GlobalVar.ERROR);
                } else {
                    newBuilder.addField(StringUtil.getPersonPrefixedString(false, "name"), name, true);
                    if (!mains.isEmpty()) {
                        newBuilder.addField(StringUtil.getPersonPrefixedString(false, "mains"), mains, true);
                    }
                    newBuilder.setColor(DatabaseBridge.getColor(discordID));
                }

                msg.editMessage(newBuilder.build()).queue();
            });
            return;
        } else if (PatternChecker.isSlippiUsername(user)) {
            builder.setTitle(GlobalVar.LOADING_EMOJI);
            builder.setColor(GlobalVar.LOADING);
            Future<List<SlippiBridge.UserEntry>> codesFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.getCodesWithActualName(user));
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                List<SlippiBridge.UserEntry> codes;
                try {
                    codes = codesFuture.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    codes = null;
                }

                EmbedBuilder newBuilder = new EmbedBuilder();
                if (codes == null || codes.size() == 0) {
                    newBuilder.setDescription("This person doesn't exist!");
                    newBuilder.setColor(GlobalVar.ERROR);
                } else {
                    if (codes.size() == 1) {
                        SlippiBridge.UserEntry entry = codes.get(0);
                        long discordID = DatabaseBridge.getDiscordIdFromConnectCode(entry.getCode());
                        newBuilder.addField(StringUtil.getPersonPrefixedString(false, "code"),
                                entry.getDisplayName() == null ? entry.getCode()
                                        : StringUtil.stringWithSlippiUsername(entry.getCode(), entry.getDisplayName()),
                                true);
                        if (discordID != -1) {
                            String mains = getMains(discordID);
                            if (!mains.isEmpty()) {
                                newBuilder.addField(StringUtil.getPersonPrefixedString(false, "mains"), mains, true);
                            }
                        }
                        newBuilder.setColor(DatabaseBridge.getColor(discordID));
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

                        String title = StringUtil.bold( codes.size() + " players are using this username:") + "\n\n";

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

    }
         */
    }

    private String getMains(long discordId) {
        return StringUtil.getMainsFormatted(DatabaseBridge.getMains(discordId));
    }

    private void outputWithDiscordId(long discordId, String retrievedCode, SlashCommandEvent e, boolean you) {
        Future<String> nameFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.getName(retrievedCode));

        e.deferReply().queue(success -> {
            String mains = getMains(discordId);

            EmbedBuilder builder = new EmbedBuilder();
            builder.addField(StringUtil.getPersonPrefixedString(you, "code"), retrievedCode, true);

            String name;

            try {
                name = nameFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception ex) {
                ex.printStackTrace();
                name = null;
            }

            builder.addField(StringUtil.getPersonPrefixedString(you, "name"), name == null ? "*No name found*" : name, true);

            if (!mains.isEmpty()) {
                builder.addField(StringUtil.getPersonPrefixedString(you, "mains"), mains, true);
            }

            builder.setColor(DatabaseBridge.getColor(discordId));
            e.getHook().sendMessageEmbeds(builder.build()).queue();
        });
    }

    /*
    private void outputWithSlippiName(String username, String retrievedCode, SlashCommandEvent e, boolean you) {
        Future<String> nameFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.getName(retrievedCode));

        e.deferReply().queue(success -> {
            String mains = getMains(discordId);

            EmbedBuilder builder = new EmbedBuilder();
            builder.addField(StringUtil.getPersonPrefixedString(you, "code"), retrievedCode, true);

            String name;

            try {
                name = nameFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception ex) {
                ex.printStackTrace();
                name = null;
            }

            builder.addField(StringUtil.getPersonPrefixedString(you, "name"), name == null ? "*No name found*" : name, true);

            if (!mains.isEmpty()) {
                builder.addField(StringUtil.getPersonPrefixedString(you, "mains"), mains, true);
            }

            builder.setColor(DatabaseBridge.getColor(discordId));
            e.getHook().sendMessageEmbeds(builder.build()).queue();
        });
    }
     */
}
