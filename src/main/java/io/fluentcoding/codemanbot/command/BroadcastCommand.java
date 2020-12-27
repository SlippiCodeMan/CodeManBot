package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.container.BroadcastContainer;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.codemancommand.AdminCodeManCommand;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BroadcastCommand extends AdminCodeManCommand {
    public static List<BroadcastMode> broadcastModes = new ArrayList<>();

    static {
        broadcastModes.add(BroadcastMode.createBroadcastModeAsync("To all", (jda) ->
                DatabaseBridge.getAllDiscordIds().stream().map(id -> jda.retrieveUserById(id).submit()).collect(Collectors.toList())
        ));
        broadcastModes.add(BroadcastMode.createBroadcastModeAsync("To all owners", (jda) ->
                jda.getGuilds().stream().map(guild -> guild.retrieveOwner().submit()).collect(Collectors.toList())
        ));
        broadcastModes.add(BroadcastMode.createBroadcastMode("To devs", (jda) ->
                Arrays.stream(GlobalVar.owners).mapToObj(id -> jda.retrieveUserById(id).complete()).collect(Collectors.toList())
        ));
    }

    public BroadcastCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void handleOnSuccess(GuildMessageReceivedEvent e) {
        if (BroadcastContainer.INSTANCE.broadcastAlreadyActive()) {
            e.getMessage().delete().queue();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(GlobalVar.SUCCESS);
            builder.setDescription("Someone already started a broadcast! Stop it before making a new one!");
            e.getAuthor().openPrivateChannel().queue(channel -> {
                channel.sendMessage(builder.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
            });

            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(GlobalVar.SUCCESS);
        builder.setDescription("Broadcast started! Who do you want to send it to?\n\n" +
                broadcastModes.stream().map(mode -> mode.getEmote() + " " + mode.getDescription()).collect(Collectors.joining("\n")));
        builder.appendDescription("\n" + GlobalVar.CANCEL_EMOJI + " Cancel broadcast");
        e.getChannel().sendMessage(builder.build()).queue(msg -> {
            BroadcastContainer.INSTANCE.broadcastHandler(e.getChannel().getIdLong(), msg.getIdLong());
            broadcastModes.stream().forEachOrdered(mode -> msg.addReaction(mode.emote).queue());
            msg.addReaction(GlobalVar.CANCEL_EMOJI).queue();
        });
    }

    @Getter
    public static class BroadcastMode {
        private static int lastDigit = 0;

        private String emote;
        private String description;
        private Function<JDA, List<User>> fetcher = null;
        private Function<JDA, List<CompletableFuture>> asyncFetcher = null;

        public static BroadcastMode createBroadcastMode(String description, Function<JDA, List<User>> fetcher) {
            BroadcastMode mode = new BroadcastMode();
            mode.emote = mode.nextAvailableDigit();
            mode.description = description;
            mode.fetcher = fetcher;

            return mode;
        }

        public static BroadcastMode createBroadcastModeAsync(String description, Function<JDA, List<CompletableFuture>> asyncFetcher) {
            BroadcastMode mode = new BroadcastMode();
            mode.emote = mode.nextAvailableDigit();
            mode.description = description;
            mode.asyncFetcher = asyncFetcher;

            return mode;
        }

        private String nextAvailableDigit() {
            lastDigit++;
            return StringUtil.getNumberedEmoji(lastDigit);
        }
    }
}
