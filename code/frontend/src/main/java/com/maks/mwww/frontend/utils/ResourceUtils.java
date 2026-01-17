package com.maks.mwww.frontend.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtils {

    public static Path customCssPath() {
        return basePath().resolve("style.css");
    }

    private static Path basePath() {
        return Paths.get(System.getProperty("user.home"), ".config", "mwww");
    }
}
