package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.Application;
import lombok.Getter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.List;
import java.util.Map;

@Getter
public abstract class DevCodeManCommand {
    private final String name;

    public DevCodeManCommand(String name) {
        this.name = Application.EXEC_MODE.getCommandPrefix() + name;
    }

    public abstract void handle(PrivateMessageReceivedEvent e);
}