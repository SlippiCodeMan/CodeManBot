package io.fluentcoding.codemanbot.listener;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.container.BroadcastContainer;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BroadcastMessageReceived extends ListenerAdapter {

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent e) {
        if (e.getAuthor().isBot())
            return;

        String message = e.getMessage().getContentStripped();
        if (message.equals("!notify") || message.startsWith("!notify ")) {
            boolean newResult = DatabaseBridge.toggleNotifications(e.getAuthor().getIdLong());

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(GlobalVar.SUCCESS);
            builder.setDescription("Notifications turned **" + (newResult ? "on" : "off") + "**!");
            e.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(builder.build()).queue());
        }
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent e) {
        if (e.getAuthor().isBot())
            return;

        if (BroadcastContainer.INSTANCE.broadcastAlreadyActive()) {
            if (e.getChannel().getIdLong() == BroadcastContainer.INSTANCE.getChannelId()) {
                e.getMessage().delete().queue();
                e.getChannel().deleteMessageById(BroadcastContainer.INSTANCE.getInitiatorMessageId()).queue();
                e.getChannel().deleteMessageById(BroadcastContainer.INSTANCE.getCurrentMessageId()).queue();
                e.getChannel().deleteMessageById(BroadcastContainer.INSTANCE.getWriteYourMessageId()).queue();

                String message = e.getMessage().getContentStripped();

                BroadcastContainer.INSTANCE.getMode().getFetcher().apply(e.getJDA()).stream().forEachOrdered(user -> {
                    if (DatabaseBridge.notifiable(user.getIdLong()))
                        user.openPrivateChannel().queue(channel -> {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setDescription(message);
                            builder.setColor(GlobalVar.SUCCESS);
                            builder.setFooter("Write **!notify** here to turn on/off notifications");
                            channel.sendMessage(builder.build()).queue();
                        });
                });

                BroadcastContainer.INSTANCE.stopBroadcast();

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(GlobalVar.SUCCESS);
                builder.setDescription("Message got sent to everyone!");
                e.getChannel().sendMessage(builder.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
            }
        }
    }
}
