package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.util.GlobalVar;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

@Getter
public abstract class AdminCodeManCommand extends CodeManCommand {
    public AdminCodeManCommand(String name, String... aliases) {
        super(null, name, aliases);
    }

    public abstract void handleOnSuccess(MessageReceivedEvent e);

    public void handle(MessageReceivedEvent e) {
        if (Arrays.stream(GlobalVar.owners).anyMatch(owner -> e.getAuthor().getIdLong() == owner))
            handleOnSuccess(e);
    }

    public String getHelpTitle() {
        return null;
    }
}
