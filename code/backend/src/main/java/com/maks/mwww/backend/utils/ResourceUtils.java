package com.maks.mwww.backend.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtils {

    public static Path tracksDirPath() {
        return basePath().resolve("local-tracks");
    }

    public static String cachePath(String subPath) {
        return cachePath().resolve(subPath).toAbsolutePath().toString();
    }

    public static Path credentialsPath() {
        return basePath().resolve("creds.json");
    }

    public static Path configFilePath() {
        return basePath().resolve("config.json");
    }

    private static Path cachePath() {
        return Paths.get(System.getProperty("user.home"), ".cache", "mwww");
    }

    private static Path basePath() {
        return Paths.get(System.getProperty("user.home"), ".config", "mwww");
    }
}
