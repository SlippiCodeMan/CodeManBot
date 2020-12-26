package io.fluentcoding.codemanbot.listener;

import io.fluentcoding.codemanbot.container.BroadcastContainer;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BroadcastMessageReceived extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (BroadcastContainer.INSTANCE.broadcastAlreadyActive()) {
            if (e.getChannel().getIdLong() == BroadcastContainer.INSTANCE.getCurrentMessageId()) {
                String message = e.getMessage().getContentStripped();

                BroadcastContainer.INSTANCE.getMode().getFetcher().apply(e.getJDA()).stream().forEachOrdered(user ->
                    user.openPrivateChannel().queue(channel -> channel.sendMessage(message))
                );

                BroadcastContainer.INSTANCE.stopBroadcast();

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(GlobalVar.SUCCESS);
                builder.setDescription("Message got sent to everyone!");
                e.getChannel().sendMessage(builder.build()).queue();
            }
        }
    }
}
