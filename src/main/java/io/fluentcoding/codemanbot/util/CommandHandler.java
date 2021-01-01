package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.util.antispam.AntiSpamContainer;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent e) {
        if (e.getAuthor().isBot())
            return;

        String msg = e.getMessage().getContentStripped();
        for (CodeManCommand command : commands) {
            if (isCommand(msg, command.getName()) ||
                    Arrays.stream(command.getAliases()).anyMatch(alias -> isCommand(msg, alias))) {
                if (!Arrays.stream(GlobalVar.owners).anyMatch(owner -> owner == e.getAuthor().getIdLong()) && !AntiSpamContainer.INSTANCE.userAllowedToAction(e.getAuthor().getIdLong())) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setDescription(StringUtil.bold("Anti-Spam protection") + "\n\nPlease wait a bit before writing the next command!");
                    builder.setColor(GlobalVar.ERROR);

                    try {
                        e.getMessage().delete().queue();
                    } catch(ErrorResponseException ex) {}

                    e.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(builder.build()).queue(temp -> {
                        temp.delete().queueAfter(1, TimeUnit.MINUTES);
                    }));
                    return;
                }

                if (command.getArgumentSet() != null) {
                    Optional<Map<String, String>> args = command.getArgumentSet().toMap(msg);

                    if (args.isPresent()) {
                        command.handle(e, args.get());
                    } else {
                        // SHOW SYNTAX ERROR
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setDescription("Syntax Error!");
                        builder.addField("Input", msg, false);
                        String correctUsageFieldTitle = "Correct Usage - () = Aliases ";
                        if (command.getArgumentSet().getNecessaryArguments().length != 0)
                            correctUsageFieldTitle += "| <> = Necessary Argument ";
                        if (command.getArgumentSet().getOptionalArguments().length != 0)
                            correctUsageFieldTitle += "| [] = Optional Argument";

                        builder.addField(correctUsageFieldTitle, command.getHelpTitle(), false);
                        builder.setColor(GlobalVar.ERROR);

                        e.getChannel().sendMessage(builder.build()).queue();
                    }
                } else {
                    command.handle(e, null);
                }
                return;
            }
        }
    }

    private boolean isCommand(String input, String command) {
        String lowercaseInput = input.toLowerCase();
        return (input.length() == command.length() && lowercaseInput.equals(command)) || lowercaseInput.startsWith(command + " ");
    }
}
