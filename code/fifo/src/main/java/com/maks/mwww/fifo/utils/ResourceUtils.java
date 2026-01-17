package com.maks.mwww.fifo.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtils {

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
