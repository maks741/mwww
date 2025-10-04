package org.maks.mwww_daemon.enumeration;

public enum FifoCommand {

    HIDE("hide"),
    SHOW("show"),
    RELOAD_STYLE("reload-style"),
    RELOAD_CONFIG("reload-config"),
    SET_SONG("set"),
    SET_SKIP_DURATION("skip-duration");

    private static final String separator = ":";

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

    public static FifoCommand build(FifoCommand command, String value) {
        return FifoCommand.fromString(command.commandName + separator + value);
    }

    public static FifoCommand build(FifoCommand command, int value) {
        return build(command, String.valueOf(value));
    }

    private static String getValue(String commandStr) {
        if (!commandStr.contains(separator)) {
            return "";
        }

        int index = commandStr.indexOf(separator);
        return commandStr.substring(index + 1);
    }

    public String getValue() {
        return commandValue;
    }

    public int getValueAsInt() {
        return Integer.parseInt(commandValue);
    }
}
