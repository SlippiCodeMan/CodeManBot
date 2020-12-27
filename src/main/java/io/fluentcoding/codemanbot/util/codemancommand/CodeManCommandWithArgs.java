package io.fluentcoding.codemanbot.util.codemancommand;

import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.GlobalVar;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public abstract class CodeManCommandWithArgs extends CodeManCommand {
    private final CodeManArgumentSet argumentSet;

    public CodeManCommandWithArgs(CodeManArgumentSet argSet, String description, String name, String... aliases) {
        super(description, name, aliases);

        this.argumentSet = argSet;
    }

    @Override
    public void handle(GuildMessageReceivedEvent e) {
        String msg = e.getMessage().getContentStripped();
        Optional<Map<String, String>> args = argumentSet.toMap(msg);

        if (args.isPresent()) {
            handle(e, args.get());
        } else {
            // SHOW SYNTAX ERROR
            EmbedBuilder builder = new EmbedBuilder();
            builder.setDescription("Syntax Error!");
            builder.addField("Input", msg, false);
            String correctUsageFieldTitle = "Correct Usage - () = Aliases ";
            if (argumentSet.getNecessaryArguments().length != 0)
                correctUsageFieldTitle += "| <> = Necessary Argument ";
            if (argumentSet.getOptionalArguments().length != 0)
                correctUsageFieldTitle += "| [] = Optional Argument";

            builder.addField(correctUsageFieldTitle, getHelpTitle(), false);
            builder.setColor(GlobalVar.ERROR);

            e.getChannel().sendMessage(builder.build()).queue();
        }
    }

    public abstract void handle(GuildMessageReceivedEvent e, Map<String, String> args);

    @Override
    public String getHelpTitle() {
        String original = super.getHelpTitle();

        if (argumentSet.getNecessaryArguments().length > 0) {
            original += " " +
                    Arrays.stream(argumentSet.getNecessaryArguments()).collect(Collectors.joining(" ", "<", ">"));
        }

        if (argumentSet.getOptionalArguments().length > 0) {
            original += " " +
                    Arrays.stream(argumentSet.getOptionalArguments()).collect(Collectors.joining(" ", "[", "]"));
        }

        return original;
    }
}
