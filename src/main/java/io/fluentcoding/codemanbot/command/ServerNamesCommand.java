package io.fluentcoding.codemanbot.command;

public class ServerNamesCommand {}

/*
public class ServerNamesCommand extends DevCodeManCommand {

    public ServerNamesCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void handleOnSuccess(GuildMessageReceivedEvent e, Map<String, String> args) {
        PagingContainer.INSTANCE.pageableMessageHandler(e.getChannel()::sendMessage,
                new PagingContainer.PageableContent(StringUtil.bold( e.getJDA().getGuilds().size() + " servers are using this bot:") + "\n\n",
                        e.getJDA().getGuilds().stream()
                                .sorted(Comparator.comparingInt(Guild::getMemberCount).reversed())
                                .map(
                                    guild -> guild.getName() +
                                            " (Region: " + (guild.getVoiceChannels().size() == 0 ? "Unknown" : guild.getVoiceChannels().get(0).getRegionRaw()) + ")" +
                                            " (Member count: " + guild.getMemberCount() + ")"
                        ).toArray(String[]::new), e.getAuthor().getIdLong()));
    }
}

 */