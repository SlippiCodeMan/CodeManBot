package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommandWithArgs;
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
                builder.addField("Your code", retrievedCode, true);
                String name = SlippiBridge.getName(retrievedCode);
                builder.addField("Your name", name, true);
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else if (e.getMessage().getMentionedMembers().size() > 0) {
            Member mentionedMember = e.getMessage().getMentionedMembers().get(0);
            String retrievedCode = DatabaseBridge.getCode(mentionedMember.getIdLong());

            if (retrievedCode == null) {
                builder.setDescription("This person didn't connect to CodeMan yet!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                String name = SlippiBridge.getName(retrievedCode);
                builder.addField("Their code", retrievedCode, true);
                builder.addField("Their name", name, true);
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else if (PatternChecker.isConnectCode(user)) {
            user = user.toUpperCase();

            String name = SlippiBridge.getName(user);

            if (name == null) {
                builder.setDescription("This person doesn't exist!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                builder.addField("Their name", name, false);
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else if (PatternChecker.isSlippiUsername(user)) {
            List<SlippiBridge.UserEntry> codes = SlippiBridge.getCodesWithActualName(user);

            if (codes == null || codes.size() == 0) {
                builder.setDescription("This person didn't connect to CodeMan yet!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                if (codes.size() == 1) {
                    SlippiBridge.UserEntry entry = codes.get(0);
                    builder.addField("Their code", entry.getDisplayName() == null ? entry.getCode() : StringUtil.stringWithSlippiUsername(entry.getCode(), entry.getDisplayName()), false);
                } else {
                    String first = codes.stream()
                            .filter(entry -> entry.getDisplayName() == null)
                            .map(entry -> entry.getCode())
                            .collect(Collectors.joining("\n"));
                    String additional = codes.stream()
                            .filter(entry -> entry.getDisplayName() != null)
                            .map(entry -> StringUtil.stringWithSlippiUsername(entry.getCode(), entry.getDisplayName()))
                            .collect(Collectors.joining("\n"));

                    builder.setDescription("**" + codes.size() + " players are using this username:**\n\n" +
                            first +
                            (additional.length() != 0 ? (first.length() != 0 ? "\n" : "") + additional : ""));
                }
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else {
            builder.setDescription("This parameter could neither get recognized as an username nor as a connect code!");
            builder.setColor(GlobalVar.ERROR);
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
