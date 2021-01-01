package io.fluentcoding.codemanbot.util.hook;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ListenerHook extends ListenerAdapter {
    private static Map<Long, Consumer<MessageReactionAddEvent>> reactionListeners = new HashMap<>();

    public static void addReactionListener(long msgId, Consumer<MessageReactionAddEvent> onReaction) {
        reactionListeners.put(msgId, onReaction);
    }
    public static void removeReactionListener(long msgId) {
        reactionListeners.remove(msgId);
    }
    public static int getActiveListenerHooks() {
        return reactionListeners.size();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        Consumer<MessageReactionAddEvent> listener = reactionListeners.get(event.getMessageIdLong());

        if (listener != null)
            listener.accept(event);
    }
}
