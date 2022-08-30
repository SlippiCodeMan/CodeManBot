package io.fluentcoding.codemanbot.util.codemancommand;

public class DeprecatedCodeManCommand {}

/*
@Getter
public class DeprecatedCodeManCommand extends CodeManCommand {
    private String newCommand;

    public DeprecatedCodeManCommand(String newCommand, String name, String... aliases) {
        super(null, name, aliases);

        this.newCommand = newCommand;
    }

    public void handle(GuildMessageReceivedEvent e, Map<String, String> args) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(GlobalVar.ERROR);
        builder.setDescription("This command is deprecated! Consider using " + StringUtil.bold(Application.EXEC_MODE.getCommandPrefix() + newCommand) + " instead!");

        e.getChannel().sendMessage(builder.build()).queue();
    }

    public String getHelpTitle() {
        return null;
    }
}

 */
