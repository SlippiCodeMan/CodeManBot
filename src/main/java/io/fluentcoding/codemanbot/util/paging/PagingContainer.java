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
import java.util.function.Function;

public enum PagingContainer {
    INSTANCE;

    private Map<Long, PageableContent> container = new HashMap<>();

    public void pageableMessageHandler(Function<MessageEmbed, MessageAction> action, PageableContent content) {
        action.apply(content.render()).queue(msg -> {
            content.react(msg, () -> container.put(msg.getIdLong(), content));
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
        private final String[] pagableContent;
        @Getter(AccessLevel.NONE)
        private int page = 0;

        public PageableContent(String prefix, String[] pagableContent) {
            this.prefix = prefix;
            this.pagableContent = pagableContent;
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
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(GlobalVar.SUCCESS);
            builder.setDescription(prefix +
                    String.join("\n", Arrays.copyOfRange(
                            pagableContent,
                            page * GlobalVar.MAX_ITEMS_PER_PAGE,
                            page * GlobalVar.MAX_ITEMS_PER_PAGE + GlobalVar.MAX_ITEMS_PER_PAGE)));
            builder.setFooter("Page " + (page + 1) + "/" + (maxPages() + 1));

            return builder.build();
        }

        public void react(Message msg, Runnable onDone) {
            List<CompletableFuture> futures = new ArrayList<>();
            if (canGoToPreviousPage() && !reactionContains(msg, GlobalVar.ARROW_LEFT))
                futures.add(msg.addReaction(GlobalVar.ARROW_LEFT).submit());
            else if (!canGoToPreviousPage() && reactionContains(msg, GlobalVar.ARROW_LEFT))
                futures.add(msg.removeReaction(GlobalVar.ARROW_LEFT).submit());
            if (canGoToNextPage() && !reactionContains(msg, GlobalVar.ARROW_RIGHT))
                futures.add(msg.addReaction(GlobalVar.ARROW_RIGHT).submit());
            else if (!canGoToNextPage() && reactionContains(msg, GlobalVar.ARROW_RIGHT))
                futures.add(msg.removeReaction(GlobalVar.ARROW_RIGHT).submit());

            CompletableFuture.allOf(futures.stream().toArray(CompletableFuture[]::new))
                    .thenAccept(unused -> onDone.run());
        }

        private boolean reactionContains(Message msg, String unicode) {
            return msg.getReactions().stream().anyMatch(reaction -> reaction.getReactionEmote().getEmoji().equals(unicode));
        }

        private int maxPages() {
            return (pagableContent.length - 1) / GlobalVar.MAX_ITEMS_PER_PAGE;
        }
    }
}
