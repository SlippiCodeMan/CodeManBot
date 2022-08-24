package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import io.fluentcoding.codemanbot.util.CommandHandler;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Map;

public class HelpCommand extends CodeManCommand {

    public HelpCommand(CommandData data) {
        super(data);
    }

    @Override
    public void handle(SlashCommandEvent e) {
        /*
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(StringUtil.bold("CodeMan")
                + " is a bot to link your slippi account with discord.\n\n"
                + StringUtil.underline(StringUtil.bold("Info:"))
                + StringUtil.bold(" (Aliases)")
                + " " + StringUtil.bold("<Necessary Argument>")
                + " " + StringUtil.bold("[Optional Argument]")
        );
        builder.setColor(GlobalVar.SUCCESS);

        for (CodeManCommand command : handler.getCommands()) {
            String helpTitle = command.getHelpTitle();

            if (helpTitle != null)
                builder.addField(StringUtil.oneLineCodeBlock(helpTitle), command.getDescription(), false);
        }

        builder.setFooter("made with " + GlobalVar.GREEN_HEART_EMOJI + " by Ananas#5903 and FluentCoding#3314");

        e.getChannel().sendMessage(builder.build()).queue();
         */
    }
}
