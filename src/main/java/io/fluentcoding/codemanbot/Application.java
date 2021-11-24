package io.fluentcoding.codemanbot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import io.fluentcoding.codemanbot.bridge.SlippiBotBridge;
import io.fluentcoding.codemanbot.command.*;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.codemancommand.DeprecatedCodeManCommand;
import io.fluentcoding.codemanbot.util.hook.ListenerHook;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.JSONException;

public class Application {
    public final static ExecutionMode EXEC_MODE =
        Arrays.stream(ExecutionMode.values())
                .filter(mode -> GlobalVar.dotenv.get("CODEMAN_EXEC_MODE").equals(mode.getDotEnvNotation()))
                .findFirst().orElse(null);
    public static JDA JDA;

    public static void main(final String[] args) throws LoginException, JSONException, URISyntaxException, IOException {
        ColorUtil.init();

        final JDABuilder builder = JDABuilder.createDefault(EXEC_MODE.getDiscordToken());

        builder.disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.CLIENT_STATUS, CacheFlag.MEMBER_OVERRIDES, CacheFlag.EMOTE);
        ActivityUpdater.update(builder);

        // EVENTS
        final CommandHandler handler = new CommandHandler(
                // USER COMMANDS
                new ConnectCommand(new CodeManArgumentSet().setNecessaryArguments("code"),
                        "Connects your slippi account by using your connect code", "connect"),
                new InfoCommand(new CodeManArgumentSet().setOptionalArguments("user").setLastArgumentVarArg(),
                        "Shows the info based off a slippi username/code or discord tag", "info", "i"),
                new WhoisCommand(new CodeManArgumentSet().setNecessaryArguments("user").setLastArgumentVarArg(),
                        "Shows the discord username based of a slippi username/code", "whois", "wi"),
                new MainCommand(new CodeManArgumentSet().setOptionalArguments("char").setLastArgumentVarArg(),
                        "Toggle a character main", "main", "mains", "m"),
                new ColorCommand(new CodeManArgumentSet().setOptionalArguments("color").setLastArgumentVarArg(),
                        "Sets the color of your info message", "color"),
                new AskCommand("Creates a netplay request", "ask", "a"),
                new DisconnectCommand("Wipes all your data from CodeMan's database", "disconnect"),

                // DEPRECATED COMMANDS
                new DeprecatedCodeManCommand("info","code", "c"),
                new DeprecatedCodeManCommand("info","name", "n"),

                // ADMIN COMMANDS
                new ServerNamesCommand("servernames"),
                new StatsCommand("stats"),
                new ReconnectCommand("reconnect")
        );

        handler.addCommand(new HelpCommand(handler, "Displays the help message", "help", "h"));
        builder.addEventListeners(
                handler,
                new ListenerHook()
        );

        JDA = builder.build();
        SlippiBotBridge.initHandler();
    }
}
