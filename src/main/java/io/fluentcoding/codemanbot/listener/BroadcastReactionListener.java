package io.fluentcoding.codemanbot.listener;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.command.BroadcastCommand;
import io.fluentcoding.codemanbot.container.BroadcastContainer;
import io.fluentcoding.codemanbot.container.PagingContainer;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
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
