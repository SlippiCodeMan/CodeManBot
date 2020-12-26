package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.codemancommand.AdminCodeManCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BroadcastCommand extends AdminCodeManCommand {
    private List<BroadcastMode> broadcastModes = new ArrayList<>();

    public BroadcastCommand(String name, String... aliases) {
        super(name, aliases);

        broadcastModes.add(new BroadcastMode(getNumberedEmoji(1), "To all owners", (jda) ->
            jda.getGuilds().stream().map(guild -> guild.getOwner().getUser()).collect(Collectors.toList())
        ));
        broadcastModes.add(new BroadcastMode(getNumberedEmoji(2), "To all connected members", (jda) ->
            DatabaseBridge.getAllDiscordIds().stream().map(id -> jda.retrieveUserById(id).complete()).collect(Collectors.toList())
        ));
        broadcastModes.add(new BroadcastMode(getNumberedEmoji(3), "To devs", (jda) ->
            Arrays.stream(GlobalVar.owners).mapToObj(id -> jda.retrieveUserById(id).complete()).collect(Collectors.toList())
        ));
    }

    @Override
    public void handleOnSuccess(MessageReceivedEvent e) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(GlobalVar.SUCCESS);
        builder.setDescription("Broadcast started! Who do you want to send it to?\n\n" +
                broadcastModes.stream().map(mode -> mode.getEmote() + " " + mode.getDescription()));
        e.getChannel().sendMessage(builder.build()).queue(msg -> {
            broadcastModes.stream().forEachOrdered(mode -> msg.addReaction(mode.emote).queue());
        });
    }

    private String getNumberedEmoji(int digit) {
        return "\\u003" + digit + "\uFE0F\u20E3";
    }

    @AllArgsConstructor
    @Getter
    public static class BroadcastMode {
        private String emote;
        private String description;
        private Function<JDA, List<User>> fetcher;
    }
}
