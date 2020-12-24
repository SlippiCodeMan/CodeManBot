package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.util.GlobalVar;
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
        builder.addField("Total memory", memoryStats.getTotalMemory() + "MiB", true);
        builder.addField("Maximum memory", memoryStats.getMaxMemory() + "MiB", true);
        builder.addField("Free memory", memoryStats.getFreeMemory() + "MiB", true);
        builder.addField("Used memory", memoryStats.getUsedMemory() + "MiB", true);
        builder.setColor(GlobalVar.SUCCESS);

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
