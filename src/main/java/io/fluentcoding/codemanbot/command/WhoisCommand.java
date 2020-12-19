package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.CodeManCommandWithArgs;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.PatternChecker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            user = user.toUpperCase();
            long discordId = DatabaseBridge.getDiscordIdFromConnectCode(user);

            // ERROR
            if (discordId == -1L) {
                builder.setDescription("This connect code has no discord user associated to it!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                User discordUser = e.getJDA().retrieveUserById(discordId).complete();
                builder.setDescription("**" + user + "** is **" + discordUser.getAsTag() + "**.");
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else if (PatternChecker.isSlippiUsername(user)) {
            List<SlippiBridge.UserEntry> codes = SlippiBridge.getCodesWithActualName(user);

            // ERROR
            if (codes == null) {
                builder.setDescription("Nobody uses this username!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                List<UserDiscordEntry> userEntries = new ArrayList<>();
                for (SlippiBridge.UserEntry entry : codes) {
                    long discordId = DatabaseBridge.getDiscordIdFromConnectCode(entry.getCode().toUpperCase());
                    if (discordId != -1)
                        userEntries.add(new UserDiscordEntry(entry.getDisplayName(), discordId));
                }

                if (userEntries.size() == 0) {
                    builder.setDescription("This connect code has no discord user associated to it!");
                    builder.setColor(GlobalVar.ERROR);
                } else if (userEntries.size() == 1) {
                    UserDiscordEntry entry = userEntries.get(0);
                    User discordUser = e.getJDA().retrieveUserById(entry.getDiscordId()).complete();
                    builder.setDescription("**" + entry.getDisplayName() + "** is **" + discordUser.getAsTag() + "**.");
                    builder.setColor(GlobalVar.SUCCESS);
                } else {
                    builder.setDescription("**" + userEntries.size() + " players are using this username:**\n" +
                            userEntries.stream().map(entry -> e.getJDA().retrieveUserById(entry.getDiscordId()).complete().getAsTag() +
                                    (entry.getDisplayName() != null ? " ***(" + entry.getDisplayName() + ")***" : "")).collect(Collectors.joining("\n")));
                }
            }
        } else {
            builder.setDescription("This parameter could neither be recognized as a username nor as a connect code!");
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
