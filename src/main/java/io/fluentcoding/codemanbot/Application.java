package io.fluentcoding.codemanbot;

import javax.security.auth.login.LoginException;

import io.fluentcoding.codemanbot.command.AskCommand;
import io.fluentcoding.codemanbot.command.CodeCommand;
import io.fluentcoding.codemanbot.command.ConnectCommand;
import io.fluentcoding.codemanbot.command.DisconnectCommand;
import io.fluentcoding.codemanbot.command.HelpCommand;
import io.fluentcoding.codemanbot.command.NameCommand;
import io.fluentcoding.codemanbot.command.WhoisCommand;
import io.fluentcoding.codemanbot.util.ActivityUpdater;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.CommandHandler;
import io.fluentcoding.codemanbot.util.ExecutionMode;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Application {
    public final static ExecutionMode EXEC_MODE =
        GlobalVar.dotenv.get("CODEMAN_EXEC_MODE").equals("prod") ? ExecutionMode.PRODUCTION : ExecutionMode.DEV;

    public static void main(final String[] args) throws LoginException {
        final JDABuilder builder = JDABuilder.createDefault(EXEC_MODE.getDiscordToken());

        builder.disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.CLIENT_STATUS, CacheFlag.MEMBER_OVERRIDES, CacheFlag.EMOTE);
        ActivityUpdater.update(builder);

        // EVENTS
        final CommandHandler handler = new CommandHandler(
                new ConnectCommand(new CodeManArgumentSet().setNecessaryArguments("code"),
                        "Connects your slippi account by using your connect code ", "connect"),
                new CodeCommand(new CodeManArgumentSet().setOptionalArguments("name").setLastArgumentVarArg(),
                        "Shows the code based of a slippi username", "code", "c"),
                new NameCommand(new CodeManArgumentSet().setOptionalArguments("code"),
                        "Shows the name based of a slippi connect code", "name", "n"),
                new WhoisCommand(new CodeManArgumentSet().setNecessaryArguments("user"),
                        "Shows the discord username based of a slippi username/connect code", "whois", "wi"),
                new AskCommand("Asks for you if someone wants you to play", "ask", "a"),
                new DisconnectCommand("Wipes all your data from CodeMan's database", "disconnect")
        );

        handler.addCommand(new HelpCommand(handler, "Displays the help message", "help", "h"));
        builder.addEventListeners(handler);

        builder.build();
    }

	@Override
	public String toString() {
		return "Application []";
	}
}
