package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.SystemUtil;
import io.fluentcoding.codemanbot.util.codemancommand.AdminCodeManCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class StatsCommand extends AdminCodeManCommand {

    public StatsCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void handleOnSuccess(GuildMessageReceivedEvent e) {
        SystemUtil.MemoryStats memoryStats = SystemUtil.memoryStats();

        EmbedBuilder builder = new EmbedBuilder();
        builder.addField("Total memory", "**" + memoryStats.getTotalMemory() + "**MiB", false);
        builder.addField("Maximum memory", "**" + memoryStats.getMaxMemory() + "**MiB", false);
        builder.addField("Free memory", "**" + memoryStats.getFreeMemory() + "**MiB", false);
        builder.addField("Used memory", "**" + memoryStats.getUsedMemory() + "**MiB", false);
        builder.addField("Discord API latency", "**" + e.getJDA().getGatewayPing() + "**ms", false);
        builder.addField("People with disabled notifications", String.valueOf(DatabaseBridge.deactivatedNotifies()), false);
        builder.setColor(GlobalVar.SUCCESS);

        e.getChannel().sendMessage(builder.build()).queue();
    }
}