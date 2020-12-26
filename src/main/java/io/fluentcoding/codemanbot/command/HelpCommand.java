package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.CommandHandler;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class HelpCommand extends CodeManCommand {
    private CommandHandler handler;

    public HelpCommand(CommandHandler handler, String description, String prefix, String... aliases) {
        super(description, prefix, aliases);
        this.handler = handler;
    }

    @Override
    public void handle(GuildMessageReceivedEvent e) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription("**CodeMan** is a bot to link your slippi account to ssbm.\n\n" +
                "__**Info:**__ **(Aliases)**, **<Necessary Argument>**, **[Optional Argument]**");
        builder.setColor(GlobalVar.SUCCESS);

        for (CodeManCommand command : handler.getCommands()) {
            String helpTitle = command.getHelpTitle();

            if (helpTitle != null)
                builder.addField(helpTitle, command.getDescription(), false);
        }

        builder.setFooter("Ananas#5903 (founder, ideas, prototype), FluentCoding#3314 (code cleaning, improvements, hosting)");

        e.getChannel().sendMessage(builder.build()).queue();
    }
}
