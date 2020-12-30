package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommandWithArgs;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.EmbedUtil;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class LobbyCommand extends CodeManCommandWithArgs {

    public LobbyCommand(CodeManArgumentSet argSet, String description, String prefix, String... aliases) {
        super(argSet, description, prefix, aliases);
    }

    @Override
    public void handle(GuildMessageReceivedEvent e, Map<String, String> args) {
        EmbedBuilder builder = new EmbedBuilder();
        String description = args.get("description");

        String code = DatabaseBridge.getCode(e.getAuthor().getIdLong());
        if (code == null) {
            builder = EmbedUtil.notConnected(builder);
        } else {
            e.getMessage().delete().queue();

            builder.setAuthor(e.getAuthor().getName(), null, e.getAuthor().getAvatarUrl());
            builder.setTitle("Searching `[1/2]`");

            if (description != null)
                builder.setDescription(description);

            builder.setColor(GlobalVar.WAITING);

            List<SSBMCharacter> characters = DatabaseBridge.getMains(e.getAuthor().getIdLong());
            if (characters != null && characters.size() != 0) {
                builder.addField("Mains", StringUtil.getMainsFormatted(characters), true);
            }

            Random random = new Random();

            builder.setFooter("ID: " + String.format("%03d", random.nextInt(1000)));
        }
        e.getChannel().sendMessage(builder.build()).queue(msg -> msg.addReaction(GlobalVar.CHECKMARK_EMOJI).queue());
    }
}
