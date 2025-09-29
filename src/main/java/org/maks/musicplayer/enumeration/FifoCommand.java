package org.maks.musicplayer.enumeration;

public enum FifoCommand {

    RELOAD_STYLE("reload-style"),
    SET_SONG("set");

    private final String commandName;
    private String commandValue;

    FifoCommand(String commandName) {
        this.commandName = commandName;
    }

    public static FifoCommand fromString(String commandStr) {
        for (FifoCommand command : values()) {
            if (commandStr.startsWith(command.commandName)) {
                command.commandValue = getValue(commandStr);
                return command;
            }
        }

        throw new IllegalArgumentException("FifoCommand not supported: " + commandStr);
    }

    private static String getValue(String commandStr) {
        if (!commandStr.contains(":")) {
            return "";
        }

        int index = commandStr.indexOf(":");
        return commandStr.substring(index + 1);
    }

    public String getValue() {
        return commandValue;
    }
}
