package io.fluentcoding.codemanbot.listener;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.command.BroadcastCommand;
import io.fluentcoding.codemanbot.container.BroadcastContainer;
import io.fluentcoding.codemanbot.container.PagingContainer;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BroadcastReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e) {
        if (e.getUser().isBot())
            return;

        List<User> users = BroadcastContainer.INSTANCE.getCachedTarget();
        if (users != null) {
            e.getChannel().retrieveMessageById(e.getMessageIdLong()).queue(msg -> msg.clearReactions().queue());
            String emoji = e.getReactionEmote().getEmoji();
            if (emoji.equals(GlobalVar.CANCEL_EMOJI)) {
                BroadcastContainer.INSTANCE.stopBroadcast();
                e.getChannel().deleteMessageById(e.getMessageIdLong()).queue();
            } else {
                String message = BroadcastContainer.INSTANCE.getMessage();
                String imageLink = BroadcastContainer.INSTANCE.getImageLink();

                BroadcastContainer.INSTANCE.stopBroadcast();
                AtomicInteger notifiedPeopleAmount = new AtomicInteger(0);

                users.stream().filter(user -> user != null).forEachOrdered(user -> {
                    System.out.println(user.getAsTag());
                    if (DatabaseBridge.notifiable(user.getIdLong())) {
                        notifiedPeopleAmount.getAndIncrement();
                        PrivateChannel channel = user.openPrivateChannel().complete();

                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setDescription(message);
                        builder.setColor(GlobalVar.SUCCESS);
                        builder.setImage(imageLink);
                        builder.setFooter(
                                "write " + Application.EXEC_MODE.getCommandPrefix() + "notify here to turn on/off notifications"
                        );

                        channel.sendMessage(builder.build()).queue();
                    }
                });

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(GlobalVar.SUCCESS);
                builder.setDescription("Message got sent to **" + users.size() + "** people!\n");
                builder.appendDescription("**" + (users.size() - notifiedPeopleAmount.get()) + "** of them blocked their notifications!");

                e.getChannel().sendMessage(builder.build()).queue();
            }

            return;
        }

        if (e.getMessageIdLong() != BroadcastContainer.INSTANCE.getCurrentMessageId())
            return;

        e.getReaction().removeReaction(e.getUser()).queue();
        String emoji = e.getReactionEmote().getEmoji();

        if (emoji.equals(GlobalVar.CANCEL_EMOJI)) {
            BroadcastContainer.INSTANCE.stopBroadcast();
        } else {
            int digit = StringUtil.getDigitOfEmoji(emoji);
            BroadcastCommand.BroadcastMode mode = BroadcastCommand.broadcastModes.get(digit - 1);
            BroadcastContainer.INSTANCE.setBroadcastMode(mode);

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(GlobalVar.SUCCESS);

            builder.setDescription("**Mode:** " + mode.getDescription());
            Message old = e.getChannel().retrieveMessageById(e.getMessageIdLong()).complete();
            old.clearReactions().queue();
            old.editMessage(builder.build()).queue();

            builder.setDescription("Write your message! The last link specified in your message will be used as the thumbnail!\n");
            builder.appendDescription("Or " + Application.EXEC_MODE.getCommandPrefix() + "cancel to cancel it!");
            e.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
