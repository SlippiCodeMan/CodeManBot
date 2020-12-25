package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.Application;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.SystemUtil;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public abstract class AdminCodeManCommand extends CodeManCommand {
    public AdminCodeManCommand(String name, String... aliases) {
        super(null, name, aliases);
    }

    public abstract void handleOnSuccess(MessageReceivedEvent e);

    public void handle(MessageReceivedEvent e) {
        if (Arrays.stream(GlobalVar.owners).anyMatch(owner -> e.getAuthor().getIdLong() == owner)) {
            for (Member member : e.getTextChannel().getMembers()) {
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
