package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import lombok.Getter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class CodeManCommand {
    private final String name;
    private final String description;
    private final String[] aliases;
    private CodeManArgumentSet argumentSet = null;

    public CodeManCommand(String description, String name, String... aliases) {
        this.description = description;
        this.name = Application.EXEC_MODE.getCommandPrefix() + name;
        this.aliases = Arrays.stream(aliases).map(original -> Application.EXEC_MODE.getCommandPrefix() + original).toArray(String[]::new);
    }

    public CodeManCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        this(description, name, aliases);

        this.argumentSet = argSet;
    }

    public abstract void handle(GuildMessageReceivedEvent e, Map<String, String> args);

    public String getHelpTitle() {
        String result = aliases.length == 0 ? name :
                name + " (" + String.join(", ", aliases) + ")";

        if (argumentSet != null) {
            if (argumentSet.getNecessaryArguments().length > 0) {
                result += " " +
                        Arrays.stream(argumentSet.getNecessaryArguments())
                                .map(necessaryArgument -> "<" + necessaryArgument + ">")
                                .collect(Collectors.joining(" "));
            }

            if (argumentSet.getOptionalArguments().length > 0) {
                result += " " +
                        Arrays.stream(argumentSet.getOptionalArguments())
                                .map(optionalArgument -> "[" + optionalArgument + "]")
                                .collect(Collectors.joining(" "));
            }
        }

        return result;
    }
}
