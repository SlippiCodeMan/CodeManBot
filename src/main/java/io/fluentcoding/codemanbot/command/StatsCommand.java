package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.SystemUtil;
import io.fluentcoding.codemanbot.util.codemancommand.AdminCodeManCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class StatsCommand extends AdminCodeManCommand {

    public StatsCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void handleOnSuccess(GuildMessageReceivedEvent e) {
        SystemUtil.MemoryStats memoryStats = SystemUtil.memoryStats();

        EmbedBuilder builder = new EmbedBuilder();
        builder.addField("Total memory", bold(memoryStats.getTotalMemory()) + "MiB", true);
        builder.addField("Maximum memory", bold(memoryStats.getMaxMemory()) + "MiB", true);
        builder.addField("Free memory", bold(memoryStats.getFreeMemory()) + "MiB", true);
        builder.addField("Used memory", bold(memoryStats.getUsedMemory()) + "MiB", true);
        builder.addField("Discord API Response time", bold(e.getJDA().getGatewayPing()) + "ms", true);
        builder.addField("Slippi API Response time", GlobalVar.LOADING_EMOJI, true);
        builder.addField("Servers", bold(e.getJDA().getGuilds().size()), true);
        builder.addField("Connected users", bold(DatabaseBridge.countDatabase()), true);
        builder.addField("Users with mains", bold(DatabaseBridge.usersWithMains()), true);
        builder.setColor(GlobalVar.SUCCESS);

        e.getChannel().sendMessage(builder.build()).queue(msg -> {
            builder.getFields().set(5, new MessageEmbed.Field("Slippi API Response time", bold(SlippiBridge.ping()) + "ms", true));

            msg.editMessage(builder.build()).queue();
        });
    }

    private String bold(double input) {
        return "**" + input + "**";
    }
    private String bold(long input) {
        return "**" + input + "**";
    }
}