package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.util.antispam.AntiSpamContainer;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommandWithArgs;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.List;

@Getter
public class CommandHandler extends ListenerAdapter {
    private List<CodeManCommand> commands;

    public CommandHandler(CodeManCommand... commands) {
        this.commands = new ArrayList<>(Arrays.asList(commands));
    }

    public void addCommand(CodeManCommand command) {
        this.commands.add(command);
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        String msg = event.getMessage().getContentStripped();
        for (CodeManCommand command : commands) {
            if (isCommand(msg, command.getName()) ||
                    Arrays.stream(command.getAliases()).anyMatch(alias -> isCommand(msg, alias))) {
                if (!AntiSpamContainer.INSTANCE.userAllowedToAction(event.getAuthor().getIdLong())) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setDescription("**Anti-Spam protection**\n\nPlease wait a bit before writing the next command!");
                    builder.setColor(GlobalVar.ERROR);

                    event.getChannel().sendMessage(builder.build()).queue();
                    return;
                }
                if (command instanceof CodeManCommandWithArgs) {
                    CodeManCommandWithArgs argsCommand = (CodeManCommandWithArgs) command;
                    Optional<Map<String, String>> args = argsCommand.getArgumentSet().toMap(msg);

                    if (args.isPresent()) {
                        argsCommand.handle(event, args.get());
                    } else {
                        // SHOW SYNTAX ERROR
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setDescription("Syntax Error!");
                        builder.addField("Input", msg, false);
                        builder.addField("Correct Usage - () = Aliases | <> = Necessary Argument | [] = Optional Argument", argsCommand.getHelpTitle(), false);
                        builder.setColor(GlobalVar.ERROR);

                        event.getChannel().sendMessage(builder.build()).queue();
                    }
                }
                command.handle(event);
                return;
            }
        }
    }

    private boolean isCommand(String input, String command) {
        String lowercaseInput = input.toLowerCase();
        return (input.length() == command.length() && lowercaseInput.equals(command)) || lowercaseInput.startsWith(command + " ");
    }
}
