package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.bridge.SlippiRankBridge;
import io.fluentcoding.codemanbot.bridge.SlippiRankBridge.RankEntry;
import io.fluentcoding.codemanbot.container.PagingContainer;
import io.fluentcoding.codemanbot.util.FeedbackUtil;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.PatternChecker;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                var threadPool = Executors.newCachedThreadPool();
                Future<List<SlippiBridge.UserEntry>> codesFuture = threadPool.submit(() -> SlippiBridge.getCodesWithActualName(username));
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
                        builder.setDescription(StringUtil.oneLineCodeBlock(username) + " does not exist!");
                    } else if (codes.size() == 1) {
                        SlippiBridge.UserEntry userEntry = codes.get(0);

                        RankEntry rank = SlippiRankBridge.getRank(userEntry.getCode());
                        long discordID = DatabaseBridge.getDiscordIdFromConnectCode(userEntry.getCode());
                        boolean you = (discordID == Objects.requireNonNull(e.getMember()).getIdLong());

                        builder.addField(StringUtil.getPersonPrefixedString(you, "code"),
                                userEntry.getDisplayName() == null ? userEntry.getCode()
                                        : StringUtil.listItemDetails(userEntry.getCode(), userEntry.getDisplayName(), null, null),
                                true);
                        if (discordID != -1) {
                            String mains = getMains(discordID);
                            if (!mains.isEmpty()) {
                                builder.addField(StringUtil.getPersonPrefixedString(you, "mains"), mains, true);
                            }
                        }
                        if (rank.hasPlayed()) {
                            builder.setFooter(StringUtil.getRankFormatted(rank, false), StringUtil.getRankImageUrl(rank));
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

                        Map<String, RankEntry> codesWithRanks = new HashMap<>();
                        final var ranks = SlippiRankBridge.getRanks(codes.stream().map(SlippiBridge.UserEntry::getCode).toArray(String[]::new));
                        for (SlippiBridge.UserEntry entry : codes) {
                            RankEntry rank = ranks.get(entry.getCode());
                            if (rank != null)
                                codesWithRanks.put(entry.getCode(), rank);
                        }

                        List<String> result = codes.stream()
                                .filter(entry -> entry.getDisplayName() == null && codesWithMains.containsKey(entry.getCode()))
                                .map(entry -> StringUtil.listItemDetails(
                                    entry.getCode(),
                                    null,
                                    codesWithMains.get(entry.getCode()),
                                    codesWithRanks.get(entry.getCode())
                                ))
                                .collect(Collectors.toList());
                        result.addAll(
                                codes.stream()
                                        .filter(entry -> entry.getDisplayName() == null && !codesWithMains.containsKey(entry.getCode()))
                                        .map(entry -> StringUtil.listItemDetails(
                                            entry.getCode(),
                                            entry.getDisplayName(),
                                            codesWithMains.get(entry.getCode()),
                                            codesWithRanks.get(entry.getCode())
                                        ))
                                        .collect(Collectors.toList())
                        );
                        result.addAll(
                                codes.stream()
                                        .filter(entry -> entry.getDisplayName() != null && codesWithMains.containsKey(entry.getCode()))
                                        .map(entry -> StringUtil.listItemDetails(
                                            entry.getCode(),
                                            null,
                                            codesWithMains.get(entry.getCode()),
                                            codesWithRanks.get(entry.getCode())
                                        ))
                                        .collect(Collectors.toList())
                        );
                        result.addAll(
                                codes.stream()
                                        .filter(entry -> entry.getDisplayName() != null && !codesWithMains.containsKey(entry.getCode()))
                                        .map(entry -> StringUtil.listItemDetails(
                                                        entry.getCode(),
                                                        entry.getDisplayName(),
                                                        null,
                                                        codesWithRanks.get(entry.getCode())
                                                )
                                        )
                                        .collect(Collectors.toList())
                        );

                        String title = StringUtil.bold(codes.size() + " players are using this username:") + "\n\n";

                        if (result.size() > GlobalVar.MAX_ITEMS_PER_PAGE) {
                            PagingContainer.INSTANCE.pageableMessageHandler(e.getHook()::editOriginalEmbeds,
                                    new PagingContainer.PageableContent(title, result.stream().toArray(String[]::new), e.getMember().getIdLong()));
                            return;
                        } else {
                            builder.setColor(GlobalVar.SUCCESS);
                            String content = String.join("\n", result);
                            builder.setDescription(title + content);
                        }
                    }
                    e.getHook().sendMessageEmbeds(builder.build()).queue();
                });
            } else {
                e.reply("Invalid username!").setEphemeral(true).queue();
            }
        } else if (e.getOption("code") != null) {
            String code = Objects.requireNonNull(e.getOption("code")).getAsString().toUpperCase();
            if (PatternChecker.isConnectCode(code)) {
                var threadPool = Executors.newCachedThreadPool();
                Future<String> nameFuture = threadPool.submit(() -> SlippiBridge.getName(code));
                Future<RankEntry> rankFuture = threadPool.submit(() -> SlippiRankBridge.getRank(code));

                e.deferReply().queue(interactionHook -> {
                    long discordID = DatabaseBridge.getDiscordIdFromConnectCode(code);
                    EmbedBuilder builder = new EmbedBuilder();

                    String name = null;
                    RankEntry rank = null;

                    boolean you = discordID == Objects.requireNonNull(e.getMember()).getIdLong();

                    try {
                        name = nameFuture.get(5, TimeUnit.SECONDS);
                        rank = rankFuture.get(5, TimeUnit.SECONDS);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (name == null) {
                        builder.setDescription(StringUtil.oneLineCodeBlock(code) + " doesn't exist!");
                        builder.setColor(GlobalVar.ERROR);
                    } else {
                        builder.addField(StringUtil.getPersonPrefixedString(you, "name"), name, true);
                        builder.setColor(DatabaseBridge.getColor(discordID));

                        if (discordID != -1) {
                            String mains = getMains(discordID);
                            if (!mains.isEmpty()) {
                                builder.addField(StringUtil.getPersonPrefixedString(you, "mains"), mains, true);
                            }
                        }
                        if (rank.hasPlayed()) {
                            builder.setFooter(StringUtil.getRankFormatted(rank, false), StringUtil.getRankImageUrl(rank));
                        }
                    }

                    e.getHook().editOriginalEmbeds(builder.build()).queue();
                });
            }
        } else {
            e.reply("Invalid connect code!").setEphemeral(true).queue();
        }
    }

    private String getMains(long discordId) {
        return StringUtil.getMainsFormatted(DatabaseBridge.getMains(discordId));
    }

    private void outputWithDiscordId(long discordId, String retrievedCode, SlashCommandEvent e, boolean you) {
        var threadPool = Executors.newCachedThreadPool();
        Future<String> nameFuture = threadPool.submit(() -> SlippiBridge.getName(retrievedCode));
        Future<RankEntry> rankFuture = threadPool.submit(() -> SlippiRankBridge.getRank(retrievedCode));

        e.deferReply().queue(success -> {
            String mains = getMains(discordId);

            EmbedBuilder builder = new EmbedBuilder();
            builder.addField(StringUtil.getPersonPrefixedString(you, "code"), retrievedCode, true);

            String name = null;
            RankEntry rank = null;

            try {
                name = nameFuture.get(5, TimeUnit.SECONDS);
                rank = rankFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            builder.addField(StringUtil.getPersonPrefixedString(you, "name"), name == null ? "*No name found*" : name, true);

            if (rank.hasPlayed()) {
                builder.setFooter(StringUtil.getRankFormatted(rank, false), StringUtil.getRankImageUrl(rank));
            }

            if (!mains.isEmpty()) {
                builder.addField(StringUtil.getPersonPrefixedString(you, "mains"), mains, true);
            }

            builder.setColor(DatabaseBridge.getColor(discordId));
            e.getHook().sendMessageEmbeds(builder.build()).queue();
        });
    }
}
