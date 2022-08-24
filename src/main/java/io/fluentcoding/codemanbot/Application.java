package io.fluentcoding.codemanbot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

import io.fluentcoding.codemanbot.bridge.SlippiBotBridge;
import io.fluentcoding.codemanbot.command.*;
import io.fluentcoding.codemanbot.util.*;
import io.fluentcoding.codemanbot.util.hook.ListenerHook;
import io.fluentcoding.codemanbot.util.ssbm.SSBMCharacter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
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
                new InfoCommand(new CommandData("info", "Show info on someone")
                        .addOption(OptionType.STRING, "username", "Slippi username", false)
                        .addOption(OptionType.STRING, "code", "Slippi code", false)
                        .addOption(OptionType.USER, "discord", "Discord user", false)),
                new MainCommand(new CommandData("main", "Add/remove a character from your mains")
                        .addOption(OptionType.STRING, "character", "Name of the character", true))
        );

        builder.addEventListeners(
                handler,
                new ListenerHook()
        );

        JDA = builder.build();

        SlippiBotBridge.initHandler();
    }
}
