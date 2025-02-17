package org.maks.musicplayer.utils;

import java.io.File;
import java.nio.file.Paths;

public class ResourceUtils {

    private static final File[] icons = loadIcons();

    private static File[] loadIcons() {
        File iconsFolder = new File(iconsFolderPath());

        File[] icons = iconsFolder.listFiles();

        if (icons == null) {
            throw new RuntimeException("Icons folder does not exist");
        }

        return icons;
    }

    public static String iconsFolderPath() {
        return resourcesFolderPath() + "/icons";
    }

    private static String resourcesFolderPath() {
        return Paths.get("./src/main/resources")
                .toAbsolutePath()
                .toString();
    }

    public static File[] icons() {
        return icons;
    }

}
