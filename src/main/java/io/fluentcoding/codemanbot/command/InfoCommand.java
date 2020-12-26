package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommandWithArgs;
import io.fluentcoding.codemanbot.util.paging.PagingContainer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfoCommand extends CodeManCommandWithArgs {

    public InfoCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(MessageReceivedEvent e, Map<String, String> args) {
        String user = args.get("user");

        EmbedBuilder builder = new EmbedBuilder();

        boolean mentionedMemberIsAuthor = e.getMessage().getMentionedMembers().size() > 0 && e.getMessage().getMentionedMembers().get(0).getIdLong() == e.getAuthor().getIdLong();
        if (user == null || mentionedMemberIsAuthor) {
            String retrievedCode = DatabaseBridge.getCode(e.getAuthor().getIdLong());

            if (retrievedCode == null) {
                builder.setDescription("You haven't connected to CodeMan yet! Take a look at **" + Application.EXEC_MODE.getCommandPrefix() + "connect**!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                String mains = DatabaseBridge.getMains(e.getAuthor().getIdLong()).stream()
                        .map(main -> "<:" + main.getName()
                                .replaceAll("\\s+", "_")
                                .replaceAll("[&.-]", "").toLowerCase()
                                + ":" + main.getEmoteId() + ">")
                        .collect(Collectors.joining(" "));

                builder.addField("Your code", retrievedCode, true);
                builder.addField("Your name", "*Loading...*", true);
                if (!mains.isEmpty()) {
                    builder.addField("Your mains", mains, true);
                }

                builder.setColor(GlobalVar.LOADING);
                e.getChannel().sendMessage(builder.build()).queue(msg -> {
                    EmbedBuilder newBuilder = new EmbedBuilder();
                    newBuilder.addField("Your code", retrievedCode, true);

                    String name = SlippiBridge.getName(retrievedCode);
                    newBuilder.addField("Your name", name, true);

                    if (!mains.isEmpty()) {
                        newBuilder.addField("Your mains", mains, true);
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
                String mains = DatabaseBridge.getMains(mentionedMember.getIdLong()).stream()
                        .map(main -> "<:" + main.getName()
                                .replaceAll("\\s+", "_")
                                .replaceAll("[&.-]", "").toLowerCase()
                                + ":" + main.getEmoteId() + ">")
                        .collect(Collectors.joining(" "));

                builder.addField("Their code", retrievedCode, true);
                builder.addField("Their name", "*Loading...*", true);
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
            builder.addField("Their name", "*Loading...*", true);
            String mains;
            if (discordID == -1) {
                mains = "";
            } else {
                mains = DatabaseBridge.getMains(discordID)
                        .stream()
                        .map(main -> "<:" + main.getName()
                                .replaceAll("\\s+", "_")
                                .replaceAll("[&.-]", "").toLowerCase()
                                + ":" + main.getEmoteId() + ">")
                        .collect(Collectors.joining(" "));
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
            builder.addField("Their code", "*Loading...*", false);
            builder.setColor(GlobalVar.LOADING);
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                List<SlippiBridge.UserEntry> codes = SlippiBridge.getCodesWithActualName(user);

                EmbedBuilder newBuilder = new EmbedBuilder();
                if (codes == null || codes.size() == 0) {
                    newBuilder.setDescription("This person didn't connect to CodeMan yet!");
                    newBuilder.setColor(GlobalVar.ERROR);
                } else {
                    if (codes.size() == 1) {
                        SlippiBridge.UserEntry entry = codes.get(0);
                        String mains = DatabaseBridge
                                .getMains(DatabaseBridge.getDiscordIdFromConnectCode(user.toUpperCase())).stream()
                                .map(main -> "<:"
                                        + main.getName().replaceAll("\\s+", "_").replaceAll("[&.-]", "").toLowerCase()
                                        + ":" + main.getEmoteId() + ">")
                                .collect(Collectors.joining(" "));
                        newBuilder.addField("Their code",
                                entry.getDisplayName() == null ? entry.getCode()
                                        : StringUtil.stringWithSlippiUsername(entry.getCode(), entry.getDisplayName()),
                                false);
                        newBuilder.setColor(GlobalVar.SUCCESS);
                    } else {
                        List<String> result = codes.stream()
                                .filter(entry -> entry.getDisplayName() == null)
                                .map(entry -> entry.getCode())
                                .collect(Collectors.toList());
                        result.addAll(
                                codes.stream()
                                        .filter(entry -> entry.getDisplayName() != null)
                                        .map(entry -> StringUtil.stringWithSlippiUsername(entry.getCode(), entry.getDisplayName()))
                                        .collect(Collectors.toList())
                        );

                        String title = "**" + codes.size() + " players are using this username:**\n\n";

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
