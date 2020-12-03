package io.fluentcoding.codemanbot;

import io.fluentcoding.codemanbot.command.*;
import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import io.fluentcoding.codemanbot.util.CodeManArgumentSet;
import io.fluentcoding.codemanbot.util.CommandHandler;
import io.fluentcoding.codemanbot.util.ExecutionMode;
import io.fluentcoding.codemanbot.util.GlobalVar;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

public class Application {
    public final static ExecutionMode EXEC_MODE = (GlobalVar.dotenv.get("CODEMAN_EXEC_MODE").equals("PROD")) ? ExecutionMode.PRODUCTION : ExecutionMode.DEV;

    public static void main(String[] args) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(EXEC_MODE.getDiscordToken());

        builder.disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.CLIENT_STATUS, CacheFlag.MEMBER_OVERRIDES, CacheFlag.EMOTE);
        builder.setActivity(Activity.playing(Application.EXEC_MODE.getCommandPrefix() + "help | " + DatabaseBridge.countDatabase() + " users"));

        // EVENTS
        CommandHandler handler = new CommandHandler(
                new ConnectCommand(new CodeManArgumentSet().setNecessaryArguments("code"),
                        "Connects you with your connect code", "connect"),
                new CodeCommand(new CodeManArgumentSet().setOptionalArguments("name").setLastArgumentVarArg(),
                        "Shows the code based of a slippi username", "code", "c"),
                new NameCommand(new CodeManArgumentSet().setOptionalArguments("code"),
                        "Shows the name based of a slippi connect code", "name", "n"),
                new WhoisCommand(new CodeManArgumentSet().setNecessaryArguments("user"),
                        "Shows the discord username based of a slippi username/connect code", "whois", "wi"),
                new AskCommand("Asks for you if someone wants you to play", "ask", "a"),
                new RemoveCommand("Removes all your data from the database", "remove", "rm")
        );

        handler.addCommand(new HelpCommand(handler, "Displays the help message", "help", "h"));
        builder.addEventListeners(handler);

        builder.build();
    }
}
