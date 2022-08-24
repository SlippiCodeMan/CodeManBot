package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.SlippiBotBridge;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Map;

public class ReconnectCommand {

}

/*
public class ReconnectCommand extends DevCodeManCommand {

    public ReconnectCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handleOnSuccess(SlashCommandEvent e) {
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

*/