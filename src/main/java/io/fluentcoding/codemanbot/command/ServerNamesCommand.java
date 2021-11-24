package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.ChallongeBridge;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBotBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.container.PagingContainer;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.SystemUtil;
import io.fluentcoding.codemanbot.util.codemancommand.DevCodeManCommand;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.hook.ListenerHook;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerNamesCommand extends DevCodeManCommand {

    public ServerNamesCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void handleOnSuccess(GuildMessageReceivedEvent e, Map<String, String> args) {
        PagingContainer.INSTANCE.pageableMessageHandler(e.getChannel()::sendMessage,
                new PagingContainer.PageableContent(StringUtil.bold( e.getJDA().getGuilds().size() + " servers are using this bot:") + "\n\n",
                        e.getJDA().getGuilds().stream()
                                .sorted(Comparator.comparingInt(Guild::getMemberCount).reversed())
                                .map(
                                    guild -> guild.getName() +
                                            " (Region: " + (guild.getVoiceChannels().size() == 0 ? "Unknown" : guild.getVoiceChannels().get(0).getRegionRaw()) + ")" +
                                            " (Member count: " + guild.getMemberCount() + ")"
                        ).toArray(String[]::new), e.getAuthor().getIdLong()));
    }
}