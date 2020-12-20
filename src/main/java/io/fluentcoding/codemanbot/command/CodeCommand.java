package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeCommand extends CodeManCommandWithArgs {

    public CodeCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(MessageReceivedEvent e, Map<String, String> args) {
        String name = args.get("name");

        EmbedBuilder builder = new EmbedBuilder();

        /* NO NAME SPECIFIED */
        if (e.getMessage().getMentionedMembers().size() > 0) {
            String code = DatabaseBridge.getCode(e.getMessage().getMentionedMembers().get(0).getIdLong());

            if (code == null) {
                builder.setDescription("This person hasn't set their code yet!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                builder.addField("Their code", code, false);
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else if (name == null) {
            String code = DatabaseBridge.getCode(e.getAuthor().getIdLong());

            if (code == null) {
                builder.setDescription("You haven't set your code yet! Take a look at **" + Application.EXEC_MODE.getCommandPrefix() + "connect**!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                builder.addField("Your code", code, false);
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else {
            if (!PatternChecker.isSlippiUsername(name)) {
                builder.setDescription("Your specified username is invalid!");
                builder.setColor(GlobalVar.ERROR);
                e.getChannel().sendMessage(builder.build()).queue();
                return;
            }

            List<SlippiBridge.UserEntry> codes = SlippiBridge.getCodesWithActualName(name);

            if (codes == null || codes.size() == 0) {
                builder.setDescription("This person hasn't set their code yet!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                if (codes.size() == 1) {
                    SlippiBridge.UserEntry entry = codes.get(0);
                    builder.addField("Their code", entry.getDisplayName() == null ? entry.getCode() : StringUtil.stringWithSlippiUsername(entry.getCode(), entry.getDisplayName()), false);
                } else {
                    builder.setDescription("**" + codes.size() + " players are using this username:**\n\n" +
                            codes.stream().filter(entry -> entry.getDisplayName() == null).map(entry -> entry.getCode()).collect(Collectors.joining("\n")) +
                            codes.stream().filter(entry -> entry.getDisplayName() != null).map(entry -> StringUtil.stringWithSlippiUsername(entry.getCode(), entry.getDisplayName())).collect(Collectors.joining("\n")));
                }
                builder.setColor(GlobalVar.SUCCESS);
            }
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
