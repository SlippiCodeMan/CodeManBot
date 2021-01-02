package io.fluentcoding.codemanbot.container;

import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.hook.ListenerHook;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public enum PagingContainer {
    INSTANCE;

    private Map<Long, Message> lastUserMessage = new HashMap<>();
    private ReentrantLock safetyLock = new ReentrantLock();

    public void pageableMessageHandler(Function<MessageEmbed, MessageAction> action, PageableContent content) {
        action.apply(content.render()).queue(msg -> {
            content.react(msg, () -> {
                safetyLock.lock();
                Message lastMessage = lastUserMessage.get(content.getAuthorId());
                if (lastMessage != null)
                    lastMessage.clearReactions().queue(unused -> ListenerHook.removeReactionListener(lastMessage.getIdLong()));
                lastUserMessage.put(content.getAuthorId(), msg);
                safetyLock.unlock();

                ListenerHook.addReactionListener(msg.getIdLong(), event -> {
                    event.getReaction().removeReaction(event.getUser()).queue();
                    if (event.getUser().getIdLong() != content.getAuthorId())
                        return;

                    String emoji = event.getReactionEmote().getEmoji();
                    if (emoji.equals(GlobalVar.ARROW_LEFT_EMOJI)) {
                        if (!content.canGoToPreviousPage())
                            return;
                        content.previousPage();
                    } else if (emoji.equals(GlobalVar.ARROW_RIGHT_EMOJI)) {
                        if (!content.canGoToNextPage())
                            return;
                        content.nextPage();
                    }
                    event.getChannel().editMessageById(event.getMessageIdLong(), content.render()).queue();
                });
            });

            msg.clearReactions().queueAfter(1, TimeUnit.HOURS, (unused) -> {
                ListenerHook.removeReactionListener(msg.getIdLong());
                safetyLock.lock();
                if (lastUserMessage.get(content.getAuthorId()).getIdLong() == msg.getIdLong())
                    lastUserMessage.remove(content.getAuthorId());
                safetyLock.unlock();
            });
        });
    }

    public static class PageableContent {
        private final String prefix;
        private final String[] pageableContent;
        @Getter
        private final long authorId;
        private int page = 0;

        public PageableContent(String prefix, String[] pageableContent, long authorId) {
            this.prefix = prefix;
            this.pageableContent = pageableContent;
            this.authorId = authorId;
        }

        public void previousPage() {
            if (canGoToPreviousPage())
                page--;
        }
        public void nextPage() {
            if (canGoToNextPage())
                page++;
        }

        public boolean canGoToPreviousPage() {
            return page != 0;
        }

        public boolean canGoToNextPage() {
            return page != maxPages();
        }

        public MessageEmbed render() {
            int toPage = page * GlobalVar.MAX_ITEMS_PER_PAGE + GlobalVar.MAX_ITEMS_PER_PAGE;

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(GlobalVar.SUCCESS);
            builder.setDescription(prefix +
                    String.join("\n", Arrays.copyOfRange(
                            pageableContent,
                            page * GlobalVar.MAX_ITEMS_PER_PAGE,
                            toPage > pageableContent.length ? pageableContent.length : toPage)));
            builder.setFooter("Page " + (page + 1) + "/" + (maxPages() + 1));

            return builder.build();
        }

        public void react(Message msg, Runnable onDone) {
            msg.addReaction(GlobalVar.ARROW_LEFT_EMOJI).queue(reaction -> {
                msg.addReaction(GlobalVar.ARROW_RIGHT_EMOJI).queue(reaction2 -> {
                    onDone.run();
                });
            });
        }

        private int maxPages() {
            return (pageableContent.length - 1) / GlobalVar.MAX_ITEMS_PER_PAGE;
        }
    }
}
