package io.fluentcoding.codemanbot.command;

import io.fluentcoding.codemanbot.util.CommandHandler;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.StringUtil;
import io.fluentcoding.codemanbot.util.codemancommand.CodeManCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class HelpCommand extends CodeManCommand {
    private final CommandHandler handler;

    public HelpCommand(CommandHandler handler, CommandData data) {
        super(data);
        this.handler = handler;
    }

    @Override
    public void handle(SlashCommandEvent e) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(StringUtil.bold("CodeMan")
                + " is a bot to link your slippi account with discord.\n\n"
                + StringUtil.underline(StringUtil.bold("Info:"))
                + " " + StringUtil.bold("<Necessary Argument>")
                + " " + StringUtil.bold("[Optional Argument]")
        );
        builder.setColor(GlobalVar.SUCCESS);

        for (CodeManCommand command : handler.getCommands()) {
            String name = command.getData().getName();
            String description = command.getData().getDescription();
            String args = command
                    .getData()
                    .getOptions()
                    .stream()
                    .map(option -> {
                        boolean isRequired = option.isRequired();
                        return (isRequired ? "<" : "[") + option.getName() + (isRequired ? ">" : "]");
                    })
                    .reduce("", (acc, el) -> acc + " " + el);

            String helpEntry = StringUtil.oneLineCodeBlock("/" + name + args);
            builder.addField(helpEntry, description, false);
        }

        builder.setFooter("made with " + GlobalVar.GREEN_HEART_EMOJI + " by Ananas#5903 and FluentCoding#3314");

        e.replyEmbeds(builder.build()).queue();
    }
}
