package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.util.SystemUtil;
import io.fluentcoding.codemanbot.util.codemancommand.AdminCodeManCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StatsCommand extends AdminCodeManCommand {

    public StatsCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void handleOnSuccess(MessageReceivedEvent e) {
        SystemUtil.MemoryStats memoryStats = SystemUtil.memoryStats();

        EmbedBuilder builder = new EmbedBuilder();
        builder.addField("Total memory", String.valueOf(memoryStats.getTotalMemory()), true);
        builder.addField("Maximum memory", String.valueOf(memoryStats.getMaxMemory()), true);
        builder.addField("Free memory", String.valueOf(memoryStats.getFreeMemory()), true);
        builder.addField("Used memory", String.valueOf(memoryStats.getUsedMemory()), true);

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
