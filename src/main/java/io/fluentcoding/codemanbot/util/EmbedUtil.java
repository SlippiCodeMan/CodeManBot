package io.fluentcoding.codemanbot.util;

import io.fluentcoding.codemanbot.Application;
import net.dv8tion.jda.api.EmbedBuilder;
import lombok.Getter;

public enum EmbedUtil {
    NOTCONNECTED("You haven't connected to CodeMan yet! Take a look at "
            + StringUtil.bold(Application.EXEC_MODE.getCommandPrefix()
            + "connect") +
            "!", false),
    ALREADYCONNECTED("You are already connected to this code !", false);

    @Getter private EmbedBuilder embed = new EmbedBuilder();

    EmbedUtil(String body, boolean success) {
        this.embed.setColor(success ? GlobalVar.SUCCESS : GlobalVar.ERROR);
        this.embed.setDescription(body);
    }
}