package io.fluentcoding.codemanbot.util.codemancommand;

public class DevCodeManCommand {
    private final String name;

    public DevCodeManCommand(String name) {
        this.name = name;
    }

}

/*
public abstract class DevCodeManCommand extends RestrictedCodeManCommand {

    public DevCodeManCommand(String name, String... aliases) {
        super((user, guild) -> Arrays.stream(GlobalVar.owners).anyMatch(owner -> user.getIdLong() == owner), "devs only", name, aliases);
    }
}

 */