package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.util.GlobalVar;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class AdminCodeManCommand extends CodeManCommand {
    public AdminCodeManCommand(String name, String... aliases) {
        super(null, name, aliases);
    }

    public abstract void handleOnSuccess(GuildMessageReceivedEvent e);

    @Override
    public void handle(GuildMessageReceivedEvent e) {
        if (Arrays.stream(GlobalVar.owners).anyMatch(owner -> e.getAuthor().getIdLong() == owner)) {
            for (Member member : e.getChannel().getMembers()) {
                if (!member.getUser().isBot() && !Arrays.stream(GlobalVar.owners).anyMatch(owner -> owner == member.getIdLong())) {
                    e.getMessage().delete().queue();

                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(GlobalVar.ERROR);
                    builder.setDescription("You aren't allowed to send admin only commands in a public channel!");
                    e.getAuthor().openPrivateChannel().queue(channel ->
                            channel.sendMessage(builder.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES))
                    );

                    return;
                }
            }
            handleOnSuccess(e);
        }
    }

    public String getHelpTitle() {
        return null;
    }
}
