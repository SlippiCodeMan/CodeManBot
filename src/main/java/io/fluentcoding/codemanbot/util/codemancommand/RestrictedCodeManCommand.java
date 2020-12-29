package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.util.GlobalVar;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Getter
public abstract class RestrictedCodeManCommand extends CodeManCommand {
    private BiPredicate<IMentionable, Guild> restriction;
    private String restrictionName;

    public RestrictedCodeManCommand(BiPredicate<IMentionable, Guild> restriction, String restrictionName, String name, String... aliases) {
        super(null, name, aliases);
        this.restriction = restriction;
        this.restrictionName = restrictionName;
    }

    public abstract void handleOnSuccess(GuildMessageReceivedEvent e);

    @Override
    public void handle(GuildMessageReceivedEvent e) {
        if (restriction.test(e.getAuthor(), e.getGuild())) {
            for (Member member : e.getChannel().getMembers()) {
                if (!member.getUser().isBot() && !restriction.test(member, e.getGuild())) {
                    e.getMessage().delete().queue();

                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(GlobalVar.ERROR);
                    builder.setDescription("You aren't allowed to send " + restrictionName + " commands in a public channel!");
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
