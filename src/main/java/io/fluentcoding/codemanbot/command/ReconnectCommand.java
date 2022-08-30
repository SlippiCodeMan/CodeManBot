package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.SlippiBotBridge;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.codemancommand.DevCodeManCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class ReconnectCommand extends DevCodeManCommand {

    public ReconnectCommand(String name) {
        super(name);
    }

    @Override
    public void handle(PrivateMessageReceivedEvent e) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(GlobalVar.SUCCESS);

        if (SlippiBotBridge.isConnected()) {
            builder.setDescription("You are already connected!");
        } else {
            SlippiBotBridge.reconnect();
            if (SlippiBotBridge.isConnected()) {
                builder.setDescription("Reconnected!");
            } else {
                builder.setDescription("Reconnect failed!");
                builder.setColor(GlobalVar.ERROR);
            }
        }

        e.getChannel().sendMessage(builder.build()).queue();
    }
}