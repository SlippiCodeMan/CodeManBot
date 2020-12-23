package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import lombok.Getter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class CodeManCommandWithArgs extends CodeManCommand {
    private final CodeManArgumentSet argumentSet;

    public CodeManCommandWithArgs(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(description, name, aliases);

        this.argumentSet = argSet;
    }

    public void handle(MessageReceivedEvent e, Map<String, String> args) {
        // Leave empty
    }

    @Override
    public String getHelpTitle() {
        String original = super.getHelpTitle();

        if (argumentSet.getNecessaryArguments().length > 0) {
            original += " " +
                    Arrays.stream(argumentSet.getNecessaryArguments()).collect(Collectors.joining(" ", "<", ">"));
        }

        if (argumentSet.getOptionalArguments().length > 0) {
            original += " " +
                    Arrays.stream(argumentSet.getOptionalArguments()).collect(Collectors.joining(" ", "[", "]"));
        }

        return original;
    }
}
