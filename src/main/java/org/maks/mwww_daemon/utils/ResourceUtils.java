package org.maks.mwww_daemon.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtils {

    public static Path songsDirPath() {
        return basePath().resolve("songs");
    }

    public static Path customCssPath() {
        return basePath().resolve("style.css");
    }

    public static Path commandsFifoPath() {
        return basePath().resolve(commandsFifoFileName());
    }

    public static String commandsFifoFileName() {
        return "commands.fifo";
    }

    private static Path basePath() {
        return Paths.get(System.getProperty("user.home"), ".config", "mwww");
    }
}
