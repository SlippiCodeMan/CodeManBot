package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.ChallongeBridge;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBotBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.SystemUtil;
import io.fluentcoding.codemanbot.util.codemancommand.DevCodeManCommand;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.hook.ListenerHook;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StatsCommand extends DevCodeManCommand {

    public StatsCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void handleOnSuccess(GuildMessageReceivedEvent e, Map<String, String> args) {
        SystemUtil.MemoryStats memoryStats = SystemUtil.memoryStats();

        create(e,
            new StatsEntry("Total memory", mb(memoryStats.getTotalMemory())),
            new StatsEntry("Maximum memory", mb(memoryStats.getMaxMemory())),
            new StatsEntry("Free memory", mb(memoryStats.getFreeMemory())),
            new StatsEntry("Used memory", mb(memoryStats.getUsedMemory())),
            new StatsEntry("Discord API Response time", e.getJDA().getGatewayPing() + "ms"),
            new StatsEntry("Slippi API Response time", () -> SlippiBridge.ping() + "ms"),
            new StatsEntry("Challonge API Response time", () -> ChallongeBridge.ping() + "ms"),
            new StatsEntry("Slippi Bot WebSocket", SlippiBotBridge.isConnected() ? "Connected" : "Disconnected"),
            new StatsEntry("Servers", e.getJDA().getGuilds().size()),
            new StatsEntry("Connected users", DatabaseBridge.countDatabase()),
            new StatsEntry("Users with mains", DatabaseBridge.usersWithMains()),
            new StatsEntry("Active ListenerHooks", ListenerHook.getActiveListenerHooks())
        );
    }

    private String mb(float input) {
        return input + "MiB";
    }

    private void create(GuildMessageReceivedEvent e, StatsEntry... entries) {
        EmbedBuilder builder = new EmbedBuilder();

        int i = 0;
        Map<Integer, Supplier> toUpdate = new HashMap<>();
        for (StatsEntry entry : entries) {
            builder.addField(
                    entry.getTitle(),
                    entry.getValueRetriever() != null ? GlobalVar.LOADING_EMOJI : String.valueOf(entry.getValue()),
                    true
            );

            if (entry.getValueRetriever() != null)
                toUpdate.put(i, entry.getValueRetriever());

            i++;
        }

        if (toUpdate.size() > 0) {
            Map<Integer, Future<String>> updateFutures = toUpdate.entrySet().stream().collect(Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> Executors.newCachedThreadPool().submit(() -> String.valueOf(entry.getValue().get()))
            ));

            builder.setColor(GlobalVar.LOADING);
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                for (Map.Entry<Integer, Future<String>> updateFutureEntry : updateFutures.entrySet()) {
                    try {
                        builder.getFields().set(
                                updateFutureEntry.getKey(),
                                new MessageEmbed.Field(
                                        builder.getFields().get(updateFutureEntry.getKey()).getName(),
                                        updateFutureEntry.getValue().get(),
                                        true
                                )
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        builder.getFields().set(
                                updateFutureEntry.getKey(),
                                new MessageEmbed.Field(
                                        builder.getFields().get(updateFutureEntry.getKey()).getName(),
                                        StringUtil.italic("Failed"),
                                        true
                                )
                        );
                    }
                }

                builder.setColor(GlobalVar.SUCCESS);
                msg.editMessage(builder.build()).queue();
            });
        } else {
            builder.setColor(GlobalVar.SUCCESS);
            e.getChannel().sendMessage(builder.build()).queue();
        }
    }

    @Getter
    private static class StatsEntry {
        private String title;
        private String value = null;
        private Supplier<String> valueRetriever = null;

        public StatsEntry(String title, String value) {
            this.title = title;
            this.value = value;
        }

        public StatsEntry(String title, float value) {
            this.title = title;
            this.value = String.valueOf(value);
        }

        public StatsEntry(String title, long value) {
            this.title = title;
            this.value = String.valueOf(value);
        }

        public StatsEntry(String title, Supplier<String> valueRetriever) {
            this.title = title;
            this.valueRetriever = valueRetriever;
        }
    }
}