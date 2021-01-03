package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.ChallongeBridge;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.ChallongeBridge.ParticipantEntry;
import io.fluentcoding.codemanbot.bridge.ChallongeBridge.TournamentEntry;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
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

        // BIG WIP

        String url = args.get("url");
        // Gonna change this when challonge/smash.gg detection will be done
        //url = url.replace("https://challonge.com/", "");
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle(GlobalVar.LOADING_EMOJI);
        builder.setColor(GlobalVar.LOADING);
        Future<TournamentEntry> tournamentFuture = Executors.newCachedThreadPool().submit(() -> ChallongeBridge.getTournament(url));
        Future<List<ParticipantEntry>> participantFuture = Executors.newCachedThreadPool().submit(() -> ChallongeBridge.getParticipants(url));
        e.getChannel().sendMessage(builder.build()).queue(msg -> {
            TournamentEntry tournament;
            List<ParticipantEntry> participants;
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
                if (tournament.getGameName() != "Super Smash Bros. Melee") {
                    newBuilder.setAuthor("Challonge", "https://challonge.com", "https://codeman.rocks/assets/challonge.png");
                    newBuilder.setTitle(tournament.getName(), "https://challonge.com/" + url);

                    String description = tournament.getDescription();
                    if (!description.isEmpty())
                        newBuilder.setDescription(StringUtil.getTextFromHtml(description));

                    newBuilder.addField("Status", StringUtil.oneLineCodeBlock(tournament.getState()), false);
                    if (participants != null) {
                        if (tournament.getState().equals("complete")) {
                            newBuilder.addField("Final Results", participants.stream()
                                    .filter(participant -> participant.getFinalRank() <= 3 && participant.getFinalRank() != 0)
                                    .map(participant -> Arrays.stream(RankEmotes.values())
                                            .filter(emote -> participant.getFinalRank() == emote.getNumber())
                                            .findFirst().orElse(null)
                                        + " "
                                        + participant.getDisplayName())
                                    .collect(Collectors.joining("\n")), false);
                        } else {
                            newBuilder.addField("Seeding", participants.stream()
                                    .filter(participant -> participant.getSeed() <= 9)
                                    .map(participant -> StringUtil.bold(participant.getSeed()
                                        + ". ")
                                        + StringUtil.separateCodeFromUsername(participant.getDisplayName()).get("username")
                                        + " "
                                        + StringUtil.getMainsFormatted(
                                            DatabaseBridge.getMains(
                                                DatabaseBridge.getDiscordIdFromConnectCode(
                                                    StringUtil.separateCodeFromUsername(
                                                        participant.getDisplayName()
                                            ).get("code")))))
                                    .collect(Collectors.joining("\n")), false);
                        }
                    }
                    newBuilder.setFooter(StringUtil.formatIsoDateAndTime(tournament.getStartsAt()));
                    newBuilder.setColor(GlobalVar.CHALLONGE);
                } else {
                    newBuilder.setDescription("Operation failed: this is not an ssbm tournament !");
                    newBuilder.setColor(GlobalVar.ERROR);
                }
            } else {
                newBuilder.setDescription("Operation failed: tournament not found !");
                newBuilder.setColor(GlobalVar.ERROR);
            }
            msg.editMessage(newBuilder.build()).queue();
        });
    }
}