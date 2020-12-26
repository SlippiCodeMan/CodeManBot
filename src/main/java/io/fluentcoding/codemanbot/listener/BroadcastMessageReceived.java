package io.fluentcoding.codemanbot.listener;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.container.BroadcastContainer;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.Application;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BroadcastMessageReceived extends ListenerAdapter {

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent e) {
        if (e.getAuthor().isBot())
            return;

        String message = e.getMessage().getContentStripped();
        if (message.equals(Application.EXEC_MODE.getCommandPrefix() + "notify") ||
                message.startsWith(Application.EXEC_MODE.getCommandPrefix() + "notify ")) {
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
                String message = e.getMessage().getContentRaw();

                BroadcastContainer.INSTANCE.stopBroadcast();
                if (message.equals(Application.EXEC_MODE.getCommandPrefix() + "cancel")) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(GlobalVar.SUCCESS);
                    builder.setDescription("Broadcast got cancelled!");
                    e.getChannel().sendMessage(builder.build()).queue();

                    return;
                }

                String link = "";
                if (e.getMessage().getAttachments().size() != 0) {
                    Message.Attachment attachment = e.getMessage().getAttachments().get(0);
                    if (attachment.isImage())
                        link = attachment.getUrl();
                }

                AtomicInteger notifiedPeopleAmount = new AtomicInteger();
                List<User> users = BroadcastContainer.INSTANCE.getMode().getFetcher().apply(e.getJDA());

                final String finalLink = link;

                users.stream().forEachOrdered(user -> {
                    if (DatabaseBridge.notifiable(user.getIdLong())) {
                        notifiedPeopleAmount.getAndIncrement();
                        user.openPrivateChannel().queue(channel -> {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setDescription(message);
                            builder.setColor(GlobalVar.SUCCESS);
                            builder.setImage(finalLink);
                            builder.setFooter(
                                    "write " + Application.EXEC_MODE.getCommandPrefix() + "notify here to turn on/off notifications"
                            );
                            channel.sendMessage(builder.build()).queue();
                        });
                    }
                });

                int notifiedPeopleAmountInt = notifiedPeopleAmount.get();

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(GlobalVar.SUCCESS);
                builder.setDescription("Message got sent to **" + notifiedPeopleAmount.get() + "** person!");
                if (users.size() != notifiedPeopleAmountInt) {
                    builder.appendDescription("\n**" + (users.size() - notifiedPeopleAmountInt) + "** didn't get notified!");
                }
                e.getChannel().sendMessage(builder.build()).queue();
            }
        }
    }
}
