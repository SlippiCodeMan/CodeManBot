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

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
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

        if (platform == Platforms.CHALLONGE) {
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
                                    .sorted(Comparator.comparingInt(ChallongeBridge.ParticipantEntry::getFinalRank))
                                    .map(participant -> {
                                        Map<String, String> seperateCodeFromUsername = StringUtil.separateCodeFromUsername(
                                                participant.getDisplayName()
                                        );
                                        String prefix = getRankingSuffix(participant.getFinalRank());
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
                                    .sorted(Comparator.comparingInt(ChallongeBridge.ParticipantEntry::getSeed))
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
        } else if (platform == Platforms.SMASHGG) {
            // DIRTY UNTIL THE SMASHGG BRIDGE IS DONE
            EmbedBuilder newBuilder = new EmbedBuilder();
            SmashggBridge.TournamentEntry tournament = SmashggBridge.getTournament(slug);

            if (tournament != null) {
                SmashggBridge.OwnerEntry owner = tournament.getOwner();
                List<SmashggBridge.EventEntry> events = tournament.getEvents();

                newBuilder.setTitle(tournament.getName(), platform.getUrl() + slug);
                newBuilder.setAuthor(owner.getName(), platform.getUrl() + owner.getSlug(), owner.getImage().isEmpty() ? null : owner.getImage());

                if (events.size() >= 1) {
                    events.stream().forEach(eventEntry -> {
                        List<SmashggBridge.ParticipantEntry> participants = eventEntry.getStandings();
                        String date = null;
                        if (events.size() > 1) {
                            Date startsAt = new Date(eventEntry.getStartAt());
                            date = "*" + (startsAt.after(new Date()) ? "Starts" : "Started") + " on: " + new SimpleDateFormat("MM/dd/yyyy").format(startsAt) + "*\n\n";
                        }

                        newBuilder.addField(
                                eventEntry.getName() + " **(" + (eventEntry.isDone() ? "Placements" : "Seeding") + "**)",
                                (date == null ? "" : date) +
                                participants.stream()
                                .map(participant -> {
                                    int number = eventEntry.isDone() ? participant.getPlacement() : participant.getSeed();
                                    String prefix = eventEntry.isDone() ? getRankingSuffix(number) : StringUtil.bold(number + ". ");

                                    return prefix + participant.getName();
                                })
                                .collect(Collectors.joining("\n")) + "\n", true);
                    });

                    if (events.size() == 1)
                        newBuilder.setTimestamp(new Date(events.get(0).getStartAt()).toInstant());
                }
                if (tournament.getImageBanner().isEmpty())
                    newBuilder.setThumbnail(tournament.getImageProfile().isEmpty() ? null : tournament.getImageProfile());
                else
                    newBuilder.setImage(tournament.getImageBanner().isEmpty() ? null : tournament.getImageBanner());
                
                newBuilder.setColor(GlobalVar.SMASHGG);
            } else {
                newBuilder.setDescription("Operation failed: Tournament not found!");
                newBuilder.setColor(GlobalVar.ERROR);
            }

            e.getChannel().sendMessage(newBuilder.build()).queue();
        }
    }

    public String getRankingSuffix(int ranking) {
        RankEmotes rankEmote = Arrays.stream(RankEmotes.values())
                .filter(emote -> ranking == emote.getNumber())
                .findFirst().orElse(null);
        return (rankEmote == null ? StringUtil.bold(ranking + (ranking == 1 ? "st" : (ranking == 2 ? "nd" : (ranking == 3 ? "rd" : "th")))) : rankEmote.getEmote()) + " ";
    }
}
