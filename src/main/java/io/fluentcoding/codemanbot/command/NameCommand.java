package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.CodeManCommandWithArgs;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.PatternChecker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

public class NameCommand extends CodeManCommandWithArgs {
    public NameCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(MessageReceivedEvent e, Map<String, String> args) {
        String code = args.get("code");

        EmbedBuilder builder = new EmbedBuilder();

        /* NO NAME SPECIFIED */
        if (code == null) {
            String retrievedCode = DatabaseBridge.getCode(e.getAuthor().getIdLong());

            if (retrievedCode == null) {
                builder.setDescription("You haven't connected yourself with CodeMan yet! Take a look at **" + Application.EXEC_MODE.getCommandPrefix() + "connect**!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                String name = SlippiBridge.getName(DatabaseBridge.getCode(e.getAuthor().getIdLong()));
                builder.addField("Your name", name, false);
                builder.setColor(GlobalVar.SUCCESS);
            }
        } else {
            code = code.toUpperCase();
            if (!PatternChecker.isConnectCode(code)) {
                builder.setDescription("Your specified connect code is invalid!");
                builder.setColor(GlobalVar.ERROR);
                e.getChannel().sendMessage(builder.build()).queue();
                return;
            }

            String name = SlippiBridge.getName(code);

            if (name == null) {
                builder.setDescription("This person doesn't exist!");
                builder.setColor(GlobalVar.ERROR);
            } else {
                builder.addField("Their name", name, false);
                builder.setColor(GlobalVar.SUCCESS);
            }
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
