package io.fluentcoding.codemanbot.listener;

import io.fluentcoding.codemanbot.container.BroadcastContainer;
import io.fluentcoding.codemanbot.container.PagingContainer;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BroadcastReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        if (event.getMessageIdLong() != BroadcastContainer.INSTANCE.getCurrentMessageId())
            return;

        event.getReaction().removeReaction(event.getUser()).queue();
        String emoji = event.getReactionEmote().getEmoji();

        if (emoji.equals(GlobalVar.CANCEL_EMOJI)) {
            BroadcastContainer.INSTANCE.stopBroadcast();
            event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
        }
    }
}
