package org.maks.mwww_daemon.shared.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtils {

    public static Path tracksDirPath() {
        return basePath().resolve("local-tracks");
    }

    public static Path customCssPath() {
        return basePath().resolve("style.css");
    }

    public static Path commandsFifoPath() {
        return basePath().resolve(commandsFifoFileName());
    }

    public static String cachePath(String subPath) {
        return cachePath().resolve(subPath).toAbsolutePath().toString();
    }

    public static Path credentialsPath() {
        return basePath().resolve("creds.json");
    }

    public static String commandsFifoFileName() {
        return "commands.fifo";
    }

    public static Path configFilePath() {
        return basePath().resolve("config.yaml");
    }

    private static Path cachePath() {
        return Paths.get(System.getProperty("user.home"), ".cache", "mwww");
    }

    private static Path basePath() {
        return Paths.get(System.getProperty("user.home"), ".config", "mwww");
    }
}
