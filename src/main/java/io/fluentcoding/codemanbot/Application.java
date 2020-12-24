package io.fluentcoding.codemanbot;

import javax.security.auth.login.LoginException;

import io.fluentcoding.codemanbot.command.*;
import io.fluentcoding.codemanbot.listener.ReactionListener;
import io.fluentcoding.codemanbot.util.ActivityUpdater;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.CommandHandler;
import io.fluentcoding.codemanbot.util.ExecutionMode;
import io.fluentcoding.codemanbot.util.GlobalVar;
import io.fluentcoding.codemanbot.util.codemancommand.DeprecatedCodeManCommand;
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
                new InfoCommand(new CodeManArgumentSet().setOptionalArguments("user or code").setLastArgumentVarArg(),
                        "Shows the info based of a slippi username/connect code", "info", "i"),
                new DeprecatedCodeManCommand("info","Shows the code based of a slippi username", "code", "c"),
                new DeprecatedCodeManCommand("info","Shows the name based of a slippi connect code", "name", "n"),
                new WhoisCommand(new CodeManArgumentSet().setNecessaryArguments("user or code"),
                        "Shows the discord username based of a slippi username/connect code", "whois", "wi"),
                new AskCommand("Asks for you if someone wants you to play", "ask", "a"),
                new DisconnectCommand("Wipes all your data from CodeMan's database", "disconnect")
        );

        handler.addCommand(new HelpCommand(handler, "Displays the help message", "help", "h"));
        builder.addEventListeners(
                handler,
                new ReactionListener()
        );

        builder.build();
    }

	@Override
	public String toString() {
		return "Application []";
	}
}
