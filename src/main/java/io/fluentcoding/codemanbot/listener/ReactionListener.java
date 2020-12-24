package io.fluentcoding.codemanbot.listener;

import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.paging.PagingContainer;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        PagingContainer.PageableContent content = PagingContainer.INSTANCE.getPageableContent(event.getMessageIdLong());

        if (content == null)
            return;

        String emoji = event.getReactionEmote().getEmoji();
        event.getReaction().removeReaction(event.getUser()).queue();
        if (emoji.equals(GlobalVar.ARROW_LEFT)) {
            PagingContainer.INSTANCE.removePageableContent(event.getMessageIdLong());
            event.getReaction().removeReaction(event.getUser()).queue();
            if (!content.canGoToPreviousPage())
                return;

            content.previousPage();
            event.getChannel().editMessageById(event.getMessageIdLong(), content.render()).queue();
        } else if (emoji.equals(GlobalVar.ARROW_RIGHT)) {
            PagingContainer.INSTANCE.removePageableContent(event.getMessageIdLong());
            if (!content.canGoToNextPage())
                return;

            content.nextPage();
            event.getChannel().editMessageById(event.getMessageIdLong(), content.render()).queue();
        }
    }
}
