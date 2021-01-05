package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.ChallongeBridge;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SmashggBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.tournament.Platforms;
import io.fluentcoding.codemanbot.util.tournament.RankEmotes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class TournamentInfoCommand extends CodeManCommand {

    public TournamentInfoCommand(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(argSet, description, name, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e, Map<String, String> args) {
        // e.getMessage().delete().queue();

        String url = args.get("url");
        //boolean isUrl = PatternChecker.isChallongeLink(url);
        String slug = url;

        // DEBUG

        /*
        if (isUrl) {
            String subdomain = PatternChecker.getSubdomain(url);
            String urlCopy = url;
            while (urlCopy.charAt(url.length() - 1) == '/')
                urlCopy = urlCopy.substring(0, urlCopy.length() - 1);

            slug = (subdomain == null ? "" : subdomain + "-") + urlCopy.substring(urlCopy.lastIndexOf('/') + 1);
        } else {
            slug = url;
        }

        Platforms platform = Arrays.stream(Platforms.values())
                .filter(item -> url.contains(item.getUrl()))
                .findFirst().orElse(null);
        */
        Platforms platform = Platforms.SMASHGG;

        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle(GlobalVar.LOADING_EMOJI);
        builder.setColor(GlobalVar.LOADING);

        if (platform.equals(Platforms.CHALLONGE)) {
            Future<ChallongeBridge.TournamentEntry> tournamentFuture = Executors.newCachedThreadPool().submit(() -> ChallongeBridge.getTournament(slug));
            Future<List<ChallongeBridge.ParticipantEntry>> participantFuture = Executors.newCachedThreadPool().submit(() -> ChallongeBridge.getParticipants(slug));
            e.getChannel().sendMessage(builder.build()).queue(msg -> {
                ChallongeBridge.TournamentEntry tournament;
                List<ChallongeBridge.ParticipantEntry> participants;
                try {
                    tournament = tournamentFuture.get();
                    participants = participantFuture.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    tournament = null;
                    participants = null;
                }

                EmbedBuilder newBuilder = new EmbedBuilder();

                if (tournament != null) {
                    newBuilder.setAuthor(platform.getName(),
                                         platform.getUrl(),
                                         "https://codeman.rocks/assets/" + platform.getName().toLowerCase() + ".png"
                    );
                    //newBuilder.setTitle(tournament.getName(), isUrl ? StringUtil.makeUrlValid(url) : platform.getUrl() + url);

                    String description = StringUtil.getTextFromHtml(tournament.getDescription());
                    if (!description.isEmpty()) {
                        if (description.length() > 300)
                            description = description.substring(0, 301) + "...";

                        newBuilder.setDescription(description);
                    }

                    if (participants != null) {
                        if (tournament.getState().equals("complete")) {
                            newBuilder.addField("Final Results", participants.stream()
                                    .filter(participant -> participant.getFinalRank() <= 5 && participant.getFinalRank() != 0)
                                    .sorted((o1,o2) -> Integer.compare(o1.getFinalRank(), o2.getFinalRank()))
                                    .map(participant -> {
                                        Map<String, String> seperateCodeFromUsername = StringUtil.separateCodeFromUsername(
                                                participant.getDisplayName()
                                        );
                                        RankEmotes rankEmote = Arrays.stream(RankEmotes.values())
                                                .filter(emote -> participant.getFinalRank() == emote.getNumber())
                                                .findFirst().orElse(null);
                                        String prefix = rankEmote == null ? StringUtil.bold(participant.getFinalRank() + "th") : rankEmote.getEmote();
                                        return prefix
                                                + " "
                                                + StringUtil.removeHardcodedSeeding(seperateCodeFromUsername.get("username"))
                                                + " "
                                                + StringUtil.getMainsFormatted(
                                                DatabaseBridge.getMains(
                                                        DatabaseBridge.getDiscordIdFromConnectCode(
                                                                seperateCodeFromUsername.get("code"))));
                                    })
                                    .collect(Collectors.joining("\n")), true);
                        } else {
                            newBuilder.addField("Seeding", participants.stream()
                                    .filter(participant -> participant.getSeed() <= 9)
                                    .sorted((o1,o2) -> Integer.compare(o1.getSeed(), o2.getSeed()))
                                    .map(participant -> {
                                        Map<String, String> seperateCodeFromUsername = StringUtil.separateCodeFromUsername(
                                                participant.getDisplayName()
                                        );

                                        return StringUtil.bold(participant.getSeed()
                                                + ". ")
                                                + StringUtil.removeHardcodedSeeding(seperateCodeFromUsername.get("username"))
                                                + " "
                                                + StringUtil.getMainsFormatted(
                                                DatabaseBridge.getMains(
                                                        DatabaseBridge.getDiscordIdFromConnectCode(
                                                                seperateCodeFromUsername.get("code"))));
                                    })
                                    .collect(Collectors.joining("\n")), true);
                        }
                    }

                    newBuilder.addField("Infos", 
                            StringUtil.bold("- Game: ") + tournament.getGameName() + "\n"
                            + StringUtil.bold("- Type: ") + tournament.getType() + "\n"
                            + StringUtil.bold("- Attendees: ") + tournament.getParticipantsCount() + "\n"
                            + StringUtil.bold("- State: ") + StringUtil.removeUnderscores(tournament.getState())
                            , true);

                    newBuilder.setFooter(StringUtil.formatIsoDateAndTime(tournament.getStartsAt()));
                    newBuilder.setColor(GlobalVar.CHALLONGE);
                } else {
                    newBuilder.setDescription("Operation failed: Tournament not found!");
                    newBuilder.setColor(GlobalVar.ERROR);
                }
                msg.editMessage(newBuilder.build()).queue();
            });
        } else if (platform.equals(Platforms.SMASHGG)) {
            // DIRTY UNTILL THE SMASHGG BRIDGE IS DONE
            EmbedBuilder newBuilder = new EmbedBuilder();
            SmashggBridge.TournamentEntry tournament = SmashggBridge.getTournament(slug);
            newBuilder.setTitle(tournament.getName());
            newBuilder.setColor(GlobalVar.SUCCESS);
            e.getChannel().sendMessage(newBuilder.build()).queue();
        }
    }
}