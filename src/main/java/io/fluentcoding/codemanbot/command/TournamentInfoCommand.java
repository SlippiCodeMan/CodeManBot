package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.ChallongeBridge;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SmashggBridge;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.entries.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.tournament.Platforms;
import io.fluentcoding.codemanbot.util.tournament.RankEmotes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.*;
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

        Future<TournamentEntry> tournamentFuture;

        if (platform == Platforms.CHALLONGE)
            tournamentFuture = Executors.newCachedThreadPool().submit(() -> ChallongeBridge.getTournament(slug));
        else if (platform == Platforms.SMASHGG)
            tournamentFuture = Executors.newCachedThreadPool().submit(() -> SmashggBridge.getTournament(slug));
        else
            tournamentFuture = null;
    
        e.getChannel().sendMessage(builder.build()).queue(msg -> {
            TournamentEntry tournament;
            try {
                tournament = tournamentFuture.get();
            } catch (Exception ex) {
                ex.printStackTrace();
                tournament = null;
            }

            List<ParticipantEntry> participants = tournament.getParticipants();
            List<EventEntry> events = tournament.getEvents();

            EmbedBuilder newBuilder = new EmbedBuilder();

            if (tournament != null) {
                newBuilder.setAuthor(platform.getName(),
                                        platform.getUrl(),
                                        "https://codeman.rocks/assets/" + platform.name().toLowerCase() + ".png"
                );
                //newBuilder.setTitle(tournament.getName(), isUrl ? StringUtil.makeUrlValid(url) : platform.getUrl() + url);

                String description = StringUtil.getTextFromHtml(tournament.getDescription());
                if (!description.isEmpty()) {
                    if (description != null) {
                        if (description.length() > 300)
                            description = description.substring(0, 301) + "...";

                        newBuilder.setDescription(description);
                    }
                }

                if (participants != null) {
                    if (tournament.getState().equals("complete")) {
                        newBuilder.addField("Final Results", participants.stream()
                                .filter(participant -> participant.getPlacement() <= 5 && participant.getPlacement() != 0)
                                .sorted(Comparator.comparingInt(ParticipantEntry::getPlacement))
                                .map(participant -> {
                                    Map<String, String> seperateCodeFromUsername = StringUtil.separateCodeFromUsername(
                                            participant.getName()
                                    );
                                    String prefix = getRankingSuffix(participant.getPlacement(), true);
                                    return prefix
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
                                .sorted(Comparator.comparingInt(ParticipantEntry::getSeed))
                                .map(participant -> {
                                    Map<String, String> seperateCodeFromUsername = StringUtil.separateCodeFromUsername(
                                            participant.getName()
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
                } else if (events != null) {
                    if (events.size() >= 1) {
                        events.stream().forEach(eventEntry -> {
                            List<ParticipantEntry> eventParticipants = eventEntry.getStandings();
                            String date = null;
                            if (events.size() > 1) {
                                Date startsAt = new Date(eventEntry.getStartAt());
                                date = "*" + (startsAt.after(new Date()) ? "Starts" : "Started") + " on: " + new SimpleDateFormat("MM/dd/yyyy").format(startsAt) + "*\n\n";
                            }

                            newBuilder.addField(
                                    eventEntry.getName() + " (" + eventEntry.getState().getHeader() + ")",
                                    (date == null ? "" : date) +
                                    eventParticipants.stream()
                                    .map(participant -> {
                                        int number = eventEntry.getState() == EventEntry.EventState.CREATED
                                                ? participant.getSeed() : participant.getPlacement();
                                        String prefix;
                                        if (eventEntry.getState() == EventEntry.EventState.CREATED) {
                                            prefix = StringUtil.bold(number + ". ");
                                        } else {
                                            prefix = getRankingSuffix(number, eventEntry.getState() == EventEntry.EventState.COMPLETED);
                                        }

                                        return prefix + participant.getName();
                                    })
                                    .collect(Collectors.joining("\n")) + "\n", true);
                        });

                        if (events.size() == 1)
                            newBuilder.setTimestamp(new Date(events.get(0).getStartAt()).toInstant());
                    }
                }

                newBuilder.addField("Infos", 
                        StringUtil.bold("- Game: ") + tournament.getGameName() + "\n"
                        + StringUtil.bold("- Type: ") + tournament.getType() + "\n"
                        + StringUtil.bold("- Attendees: ") + tournament.getParticipantsCount() + "\n"
                        + StringUtil.bold("- State: ") + StringUtil.removeUnderscores(tournament.getState())
                        , true);

                newBuilder.setFooter(StringUtil.fromatDate(tournament.getStartAt()));
                newBuilder.setColor(platform.getColor());
            } else if (tournament == null) {
                newBuilder.setDescription("Operation failed: Tournament not found!");
                newBuilder.setColor(GlobalVar.ERROR);
            }
            msg.editMessage(newBuilder.build()).queue();
        });
    }

    public String getRankingSuffix(int ranking, boolean useEmote) {
        RankEmotes rankEmote = useEmote ? Arrays.stream(RankEmotes.values())
                .filter(emote -> ranking == emote.getNumber())
                .findFirst().orElse(null) : null;
        return (rankEmote == null ? StringUtil.bold(ranking + (ranking == 1 ? "st" : (ranking == 2 ? "nd" : (ranking == 3 ? "rd" : "th")))) : rankEmote.getEmote()) + " ";
    }
}
