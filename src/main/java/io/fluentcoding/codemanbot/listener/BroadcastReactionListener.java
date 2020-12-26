package io.fluentcoding.codemanbot.listener;

import io.fluentcoding.codemanbot.command.BroadcastCommand;
import io.fluentcoding.codemanbot.container.BroadcastContainer;
import io.fluentcoding.codemanbot.container.PagingContainer;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BroadcastReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e) {
        if (e.getUser().isBot())
            return;

        if (e.getMessageIdLong() != BroadcastContainer.INSTANCE.getCurrentMessageId())
            return;

        e.getReaction().removeReaction(e.getUser()).queue();
        String emoji = e.getReactionEmote().getEmoji();

        if (emoji.equals(GlobalVar.CANCEL_EMOJI)) {
            BroadcastContainer.INSTANCE.stopBroadcast();
            e.getChannel().deleteMessageById(BroadcastContainer.INSTANCE.getInitiatorMessageId()).queue();
            e.getChannel().deleteMessageById(e.getMessageIdLong()).queue();
        } else {
            int digit = StringUtil.getDigitOfEmoji(emoji);
            BroadcastCommand.BroadcastMode mode = BroadcastCommand.broadcastModes.get(digit - 1);
            BroadcastContainer.INSTANCE.setBroadcastMode(mode);

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(GlobalVar.SUCCESS);

            builder.setDescription("**Mode:** " + mode.getDescription());
            e.getChannel().retrieveMessageById(e.getMessageIdLong()).queue(msg -> {
                msg.clearReactions().queue();
                msg.editMessage(builder.build()).queue();
            });

            builder.setDescription("Write your message!");
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                BroadcastContainer.INSTANCE.setWriteYourMessageId(msg.getIdLong());
            });
        }
    }
}
