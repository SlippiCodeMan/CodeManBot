package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.Application;
import net.dv8tion.jda.api.EmbedBuilder;

public class EmbedUtil {
    public static EmbedBuilder notConnected(EmbedBuilder builder) {
        builder.setColor(GlobalVar.ERROR);
        builder.setDescription("You haven't connected to CodeMan yet! Take a look at "
                + StringUtil.bold(Application.EXEC_MODE.getCommandPrefix() + "connect")
                + "!");
        return builder;
    }

    public static EmbedBuilder alreadyConnected(EmbedBuilder builder) {
        builder.setColor(GlobalVar.ERROR);
        builder.setDescription("You are already connected to this code !");
        return builder;
    }
}
