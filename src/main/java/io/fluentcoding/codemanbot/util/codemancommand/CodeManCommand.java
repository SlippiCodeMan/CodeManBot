package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.Application;
import lombok.Getter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

@Getter
public class CodeManCommand {
    private final String name;
    private final String description;
    private final String[] aliases;

    public CodeManCommand(String description, String name, String... aliases) {
        this.description = description;
        this.name = Application.EXEC_MODE.getCommandPrefix() + name;
        this.aliases = Arrays.stream(aliases).map(original -> Application.EXEC_MODE.getCommandPrefix() + original).toArray(String[]::new);
    }

    public void handle(MessageReceivedEvent e) {
        // Leave empty
    }

    public String getHelpTitle() {
        return aliases.length == 0 ? name :
                name + " (" + String.join(", ", aliases) + ")";
    }
}
