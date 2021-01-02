package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.ChallongeBridge;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.bridge.SlippiBridge;
import io.fluentcoding.codemanbot.bridge.ChallongeBridge.ParticipantEntry;
import io.fluentcoding.codemanbot.bridge.ChallongeBridge.TournamentEntry;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.container.PagingContainer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
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

        TournamentEntry tournament = ChallongeBridge.getTournament(url);
        if (tournament != null) {
            List<ParticipantEntry> participants = ChallongeBridge.getParticipants(url);

            builder.setAuthor("Challonge", "https://challonge.com", "https://codeman.rocks/assets/challonge.png");
            builder.setTitle(tournament.getName(), "https://challonge.com/" + url);

            String description = tournament.getDescription();
            if (!description.isEmpty())
                builder.setDescription(StringUtil.getTextFromHtml(description));

            builder.addField("Status", tournament.getState(), false);
            if (participants != null) {
                if (tournament.getState().equals("complete")) {
                    builder.addField("Attendees", participants.stream()
                            .map(participant -> participant.getFinalRank()
                                + " | "
                                + participant.getDisplayName())
                            .collect(Collectors.joining("\n")), false);
                } else {
                    builder.addField("Attendees", participants.stream()
                            .map(participant -> participant.getSeed()
                                + " | "
                                + participant.getDisplayName())
                            .collect(Collectors.joining("\n")), false);
                }
            }
            //builder.setFooter(StringUtil.formatIsoDateAndTime(tournament.getStartsAt()));

            builder.setColor(GlobalVar.CHALLONGE);
        } else {
            builder.setDescription("Operation failed: tournament not found !");
            builder.setColor(GlobalVar.ERROR);
        }
        e.getChannel().sendMessage(builder.build()).queue();
    }

}
