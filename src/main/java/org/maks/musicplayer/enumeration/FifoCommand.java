package org.maks.musicplayer.enumeration;

public enum FifoCommand {

    RELOAD_STYLE("reload-style"),
    SET_SONG("set");

    private final String commandName;

    FifoCommand(String commandName) {
        this.commandName = commandName;
    }

    public String getValue(String s) {
        if (!s.contains(":")) {
            throw new IllegalStateException("Command does not have a value");
        }

        int index = s.indexOf(":");
        return s.substring(index + 1);
    }

    public static FifoCommand fromString(String s) {
        for (FifoCommand command : values()) {
            if (s.startsWith(command.commandName)) {
                return command;
            }
        }

        throw new IllegalArgumentException("FifoCommand not supported: " + s);
    }
}
