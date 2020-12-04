package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.CodeManCommandWithArgs;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.PatternChecker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
            List<String> codes = SlippiBridge.getCode(user);

            // ERROR
            if (codes == null) {
                builder.setDescription("Nobody uses this username!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                List<Long> discordIds = new ArrayList<>();
                for (String code : codes) {
                    code = code.toUpperCase();
                    long discordId = DatabaseBridge.getDiscordIdFromConnectCode(code);
                    if (discordId != -1)
                        discordIds.add(discordId);
                }

                if (discordIds.size() == 0) {
                    builder.setDescription("This connect code has no discord user associated to it!");
                    builder.setColor(GlobalVar.ERROR);
                } else if (discordIds.size() == 1) {

                    User discordUser = e.getJDA().retrieveUserById(discordIds.get(0)).complete();
                    builder.setDescription("**" + user + "** is **" + discordUser.getAsTag() + "**.");
                    builder.setColor(GlobalVar.SUCCESS);
                } else {
                    System.out.println(discordIds.get(0));
                    builder.setDescription("**" + codes.size() + " players are using this username:**\n" +
                            discordIds.stream().map(discordId -> e.getJDA().retrieveUserById(discordId).complete().getAsTag()).collect(Collectors.joining("\n")));
                }
            }
        } else {
            builder.setDescription("This parameter could neither be recognized as a username nor as a connect code!");
            builder.setColor(GlobalVar.ERROR);
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
