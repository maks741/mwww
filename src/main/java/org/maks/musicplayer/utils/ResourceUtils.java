package org.maks.musicplayer.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtils {

    private static final File[] icons = loadIcons();

    private static File[] loadIcons() {
        Path iconsFolderPath = iconsFolderPath();
        File iconsFolder = iconsFolderPath.toFile();
        File[] icons = iconsFolder.listFiles();

        if (icons == null) {
            throw new RuntimeException("Icons folder does not exist");
        }

        return icons;
    }

    public static Path iconsFolderPath() {
        return Paths.get("src", "main", "resources", "icons");
    }

    public static Path songsFolderPath() {
        return Paths.get("songs");
    }

    public static File[] icons() {
        return icons;
    }

}
