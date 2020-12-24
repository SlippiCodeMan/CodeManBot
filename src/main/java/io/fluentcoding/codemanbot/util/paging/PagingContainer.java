package io.fluentcoding.codemanbot.util.paging;

import io.fluentcoding.codemanbot.util.GlobalVar;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public enum PagingContainer {
    INSTANCE;

    private Map<Long, PageableContent> container = new HashMap<>();

    public void pageableMessageHandler(Function<MessageEmbed, MessageAction> action, PageableContent content) {
        action.apply(content.render()).queue(msg -> {
            content.react(msg, () -> container.put(msg.getIdLong(), content));
            msg.clearReactions().queueAfter(1, TimeUnit.HOURS, (unused) -> {
                container.remove(msg.getIdLong(), content);
            });
        });
    }

    public PageableContent getPageableContent(Long messageId) {
        return container.get(messageId);
    }

    public void addPageableContent(Long messageId, PageableContent content) {
        container.put(messageId, content);
    }

    public void removePageableContent(Long messageId) {
        container.remove(messageId);
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
            CompletableFuture[] futures = new CompletableFuture[] {
                    msg.addReaction(GlobalVar.ARROW_LEFT).submit(),
                    msg.addReaction(GlobalVar.ARROW_RIGHT).submit()
            };

            CompletableFuture.allOf(futures).thenAccept(unused -> onDone.run());
        }

        private int maxPages() {
            return (pageableContent.length - 1) / GlobalVar.MAX_ITEMS_PER_PAGE;
        }
    }
}
