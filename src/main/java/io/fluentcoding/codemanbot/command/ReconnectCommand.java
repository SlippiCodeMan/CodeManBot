package io.fluentcoding.codemanbot.command;

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