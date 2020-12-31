package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.SystemUtil;
import io.fluentcoding.codemanbot.util.codemancommand.DevCodeManCommand;
import io.fluentcoding.codemanbot.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StatsCommand extends DevCodeManCommand {

    public StatsCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void handleOnSuccess(GuildMessageReceivedEvent e) {
        SystemUtil.MemoryStats memoryStats = SystemUtil.memoryStats();

        EmbedBuilder builder = new EmbedBuilder();
        builder.addField("Total memory", StringUtil.bold(memoryStats.getTotalMemory()) + "MiB", true);
        builder.addField("Maximum memory", StringUtil.bold(memoryStats.getMaxMemory()) + "MiB", true);
        builder.addField("Free memory", StringUtil.bold(memoryStats.getFreeMemory()) + "MiB", true);
        builder.addField("Used memory", StringUtil.bold(memoryStats.getUsedMemory()) + "MiB", true);
        builder.addField("Discord API Response time", StringUtil.bold(e.getJDA().getGatewayPing()) + "ms", true);
        builder.addField("Slippi API Response time", GlobalVar.LOADING_EMOJI, true);
        builder.addField("Servers", StringUtil.bold(e.getJDA().getGuilds().size()), true);
        builder.addField("Connected users", StringUtil.bold(DatabaseBridge.countDatabase()), true);
        builder.addField("Users with mains", StringUtil.bold(DatabaseBridge.usersWithMains()), true);
        builder.setColor(GlobalVar.SUCCESS);

        Future<Long> pingFuture = Executors.newCachedThreadPool().submit(() -> SlippiBridge.ping());
        e.getChannel().sendMessage(builder.build()).queue(msg -> {
            try {
                builder.getFields().set(5, new MessageEmbed.Field("Slippi API Response time", StringUtil.bold(pingFuture.get()) + "ms", true));
            } catch(Exception ex) {
                builder.getFields().set(5, new MessageEmbed.Field("Slippi API Response time", "*Failed*", true));
            }
            msg.editMessage(builder.build()).queue();
        });
    }
}