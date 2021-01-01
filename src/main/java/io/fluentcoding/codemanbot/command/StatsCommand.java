package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.SystemUtil;
import io.fluentcoding.codemanbot.util.codemancommand.DevCodeManCommand;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.hook.ListenerHook;
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

        create(Map.of(
                "Total memory", mb(memoryStats.getTotalMemory()),
                "Maximum memory", mb(memoryStats.getMaxMemory()),
                "Free memory", mb(memoryStats.getFreeMemory()),
                "Used memory", mb(memoryStats.getUsedMemory()),
                "Discord API Response time", e.getJDA().getGatewayPing() + "ms",
                "Slippi API Response time", (Supplier) () -> SlippiBridge.ping() + "ms",
                "Servers", e.getJDA().getGuilds().size(),
                "Connected users", DatabaseBridge.countDatabase(),
                "Users with mains", DatabaseBridge.usersWithMains(),
                "Active ListenerHooks", ListenerHook.getActiveListenerHooks()
        ), e);
    }

    private String mb(float input) {
        return input + "MiB";
    }

    private void create(Map<String, ?> values, GuildMessageReceivedEvent e) {
        EmbedBuilder builder = new EmbedBuilder();

        int i = 0;
        Map<Integer, Supplier> toUpdate = new HashMap<>();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            builder.addField(
                    entry.getKey(),
                    entry.getValue() instanceof Supplier ? GlobalVar.LOADING_EMOJI : String.valueOf(entry.getValue()),
                    true
            );

            if (entry.getValue() instanceof Supplier)
                toUpdate.put(i, (Supplier) entry.getValue());

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
                                        StringUtil.bold(updateFutureEntry.getValue().get()),
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
}