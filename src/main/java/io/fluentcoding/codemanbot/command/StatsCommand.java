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
        builder.addField("Total memory", bold(memoryStats.getTotalMemory()) + "MiB", false);
        builder.addField("Maximum memory", bold(memoryStats.getMaxMemory()) + "MiB", false);
        builder.addField("Free memory", bold(memoryStats.getFreeMemory()) + "MiB", false);
        builder.addField("Used memory", bold(memoryStats.getUsedMemory()) + "MiB", false);
        builder.addField("Discord API latency", bold(e.getJDA().getGatewayPing()) + "ms", false);
        builder.addField("Servers", bold(e.getJDA().getGuilds().size()), false);
        builder.addField("Users with mains", bold(DatabaseBridge.usersWithMains()), false);
        builder.setColor(GlobalVar.SUCCESS);

        e.getChannel().sendMessage(builder.build()).queue();
    }

    private String bold(String input) {
        return "**" + input + "**";
    }
    private String bold(double input) {
        return "**" + input + "**";
    }
    private String bold(long input) {
        return "**" + input + "**";
    }
}