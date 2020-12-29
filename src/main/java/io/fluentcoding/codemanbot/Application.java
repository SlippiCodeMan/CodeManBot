package io.fluentcoding.codemanbot;

import javax.security.auth.login.LoginException;

import io.fluentcoding.codemanbot.command.*;
import io.fluentcoding.codemanbot.listener.PagingReactionListener;
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
                // USER COMMANDS
                new ConnectCommand(new CodeManArgumentSet().setNecessaryArguments("code"),
                        "Connects your slippi account by using your connect code ", "connect"),
                new InfoCommand(new CodeManArgumentSet().setOptionalArguments("user").setLastArgumentVarArg(),
                        "Shows the info based off a slippi username/code or discord tag", "info", "i"),
                new WhoisCommand(new CodeManArgumentSet().setNecessaryArguments("user").setLastArgumentVarArg(),
                        "Shows the discord username based of a slippi username/code", "whois", "wi"),
                new MainCommand(new CodeManArgumentSet().setOptionalArguments("char").setLastArgumentVarArg(),
                        "Toggle a character main", "main", "mains", "m"),
                new AskCommand("Asks for you if someone wants you to play", "ask", "a"),
                new DisconnectCommand("Wipes all your data from CodeMan's database", "disconnect"),

                // DEPRECATED COMMANDS
                new DeprecatedCodeManCommand("info","code", "c"),
                new DeprecatedCodeManCommand("info","name", "n"),

                // ADMIN COMMANDS
                new StatsCommand("stats")
        );

        handler.addCommand(new HelpCommand(handler, "Displays the help message", "help", "h"));
        builder.addEventListeners(
                handler,
                new PagingReactionListener()
        );

        builder.build();
    }
}
