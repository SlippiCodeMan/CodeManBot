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
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class BroadcastReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e) {
        if (e.getUser().isBot())
            return;

        if (BroadcastContainer.INSTANCE.getCachedTarget() != null || BroadcastContainer.INSTANCE.getCachedFetchingStrategy() != null) {
            e.getChannel().retrieveMessageById(e.getMessageIdLong()).queue(msg -> msg.clearReactions().queue());
            String emoji = e.getReactionEmote().getEmoji();
            if (emoji.equals(GlobalVar.CANCEL_EMOJI)) {
                BroadcastContainer.INSTANCE.stopBroadcast();
                e.getChannel().deleteMessageById(e.getMessageIdLong()).queue();
            } else {
                BroadcastContainer.INSTANCE.stopBroadcast();
                AtomicInteger notifiedPeopleAmount = new AtomicInteger(0);

                int amount = BroadcastContainer.INSTANCE.getCachedTarget() != null ?
                        BroadcastContainer.INSTANCE.getCachedTarget().size() :
                        BroadcastContainer.INSTANCE.getCachedFetchingStrategy().size();

                List<CompletableFuture> asyncActions = new ArrayList<>();
                if (BroadcastContainer.INSTANCE.getCachedTarget() != null) {
                    BroadcastContainer.INSTANCE.getCachedTarget().stream().filter(user -> user != null).forEachOrdered(user -> {
                        CompletableFuture<Void> action = notifyUserIfNotifiable(user, notifiedPeopleAmount);
                        if (action != null) {
                            asyncActions.add(action);
                        }
                    });
                } else {
                    for (RestAction<User> restAction : BroadcastContainer.INSTANCE.getCachedFetchingStrategy()) {
                        restAction.submit().thenAccept(user -> {
                            if (user != null) {
                                CompletableFuture<Void> action = notifyUserIfNotifiable(user, notifiedPeopleAmount);
                                if (action != null) {
                                    asyncActions.add(action);
                                }
                            }
                        });
                    }
                }

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(GlobalVar.LOADING);
                builder.setDescription("Message will get sent to **" + amount + "** people! Please wait a bit!\n");
                builder.appendDescription("**" + (amount - notifiedPeopleAmount.get()) + "** of them blocked their notifications!");

                Message old = e.getChannel().sendMessage(builder.build()).complete();
                CompletableFuture.allOf(asyncActions.toArray(CompletableFuture[]::new)).thenAccept(unused -> {
                    builder.setColor(GlobalVar.SUCCESS);
                    builder.setDescription("Message got sent to **" + amount + "** people!\n");
                    builder.appendDescription("**" + (amount - notifiedPeopleAmount.get()) + "** of them blocked their notifications!");

                    e.getChannel().sendMessage(builder.build()).queue();
                });
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

    private CompletableFuture<Void> notifyUserIfNotifiable(User user, AtomicInteger notifiedPeopleAmount) {
        if (DatabaseBridge.notifiable(user.getIdLong())) {
            notifiedPeopleAmount.getAndIncrement();
            return user.openPrivateChannel().submit().thenAccept(channel -> {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setDescription(BroadcastContainer.INSTANCE.getMessage());
                builder.setImage(BroadcastContainer.INSTANCE.getImageLink());
                builder.setColor(GlobalVar.SUCCESS);
                builder.setFooter(
                        "write " + Application.EXEC_MODE.getCommandPrefix() + "notify here to turn on/off notifications"
                );

                channel.sendMessage(builder.build()).queue();
            });
        } else {
            return null;
        }
    }
}
