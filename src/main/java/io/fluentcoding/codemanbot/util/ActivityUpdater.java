package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.bridge.DatabaseBridge;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class ActivityUpdater {

    public static void update(JDABuilder builder) {
        builder.setActivity(getActivityStatus(DatabaseBridge.countDatabase()));
    }

    public static void update(JDA jda) {
        jda.getPresence().setActivity(getActivityStatus(DatabaseBridge.countDatabase()));
    }

    private static Activity getActivityStatus(long count) {
        return Activity.playing("/help | " + count + " users");
    }
}
