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
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
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

                    try {
                        event.getMessage().delete().queue();
                    } catch(ErrorResponseException e) {}

                    event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(builder.build()).queue(temp -> {
                        temp.delete().queueAfter(1, TimeUnit.MINUTES);
                    }));
                    return;
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
