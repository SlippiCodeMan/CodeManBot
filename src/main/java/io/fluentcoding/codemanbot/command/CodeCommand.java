package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.CodeManCommandWithArgs;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.PatternChecker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Map;

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
                builder.setDescription("This person hasn't set his code yet!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                builder.addField("Their code", code, false);
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else if (name == null) {
            String code = DatabaseBridge.getCode(e.getAuthor().getIdLong());

            if (code == null) {
                builder.setDescription("You haven't set your code yet!");
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

            List<String> codes = SlippiBridge.getCode(name);

            if (codes == null) {
                builder.setDescription("This person hasn't set his code yet!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                if (codes.size() == 1) {
                    builder.addField("Their code", codes.get(0), false);
                    builder.setColor(GlobalVar.SUCCESS);
                } else {
                    builder.setDescription("**" + codes.size() + " players are using this name:**\n\n" + String.join("\n", codes));
                    builder.setColor(GlobalVar.SUCCESS);
                }
            }
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
