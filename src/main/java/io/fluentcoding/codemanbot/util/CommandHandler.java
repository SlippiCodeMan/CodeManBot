package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public class CommandHandler extends ListenerAdapter {
    private final List<CodeManCommand> commands;

    public CommandHandler(CodeManCommand... commands) {
        this.commands = new ArrayList<>(Arrays.asList(commands));
    }

    public void addCommand(CodeManCommand command) {
        this.commands.add(command);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent e) {
        // Temporary measures

        List<String> legacyCommands = Arrays.asList(
                "info", "i",
                "connect", "c",
                "ask", "a",
                "whois", "wi",
                "main", "mains", "m",
                "color",
                "disconnect", "d",
                "help", "h"
        );

        if (e.getAuthor().isBot())
            return;

        String msg = e.getMessage().getContentStripped();

        for (String legacyCommand: legacyCommands) {
            String prefixedLegacyCommand = Application.EXEC_MODE.getCommandPrefix() + legacyCommand;
            if (isLegacyCommand(msg, prefixedLegacyCommand)) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(GlobalVar.ERROR);
                builder.setDescription(
                        "Most verified bots had to migrate to slash command due to discord's new policies, try using " + StringUtil.oneLineCodeBlock("/<command-name>")
                );
                e.getChannel().sendMessage(builder.build()).queue();
            }
        }
    }

    @Override
    public void onReady(@Nonnull ReadyEvent e) {
        // Register slash commands
        if (Application.EXEC_MODE == ExecutionMode.DEV) {
            Guild target = Objects.requireNonNull(e.getJDA().getGuildById(GlobalVar.TEST_SERVER_ID));
            commands.forEach(codeManCommand ->
                    target
                            .upsertCommand(codeManCommand.getData())
                            .queue()
            );
        } else {
            commands.forEach(codeManCommand ->
                    e.getJDA()
                            .upsertCommand(codeManCommand.getData())
                            .queue()
            );
        }
    }

    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent e) {
        for (CodeManCommand command : commands) {
            if (command.getData().getName().equals(e.getName())) {
                command.handle(e);
            }
        }
    }

    private boolean isLegacyCommand(String input, String command) {
        String lowercaseInput = input.toLowerCase();
        return (input.length() == command.length() && lowercaseInput.equals(command)) || lowercaseInput.startsWith(command + " ");
    }
}
