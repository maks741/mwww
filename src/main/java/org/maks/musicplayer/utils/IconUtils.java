package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.maks.musicplayer.enumeration.Icon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class IconUtils {

    public static Image image(Icon icon) {
        return new Image(iconPath(icon));
    }

    private static String iconPath(Icon icon) {
        Path iconsFolderPath = Paths.get("src", "main", "resources", "icons");
        String iconName = icon.toString();

        try (Stream<Path> icons = Files.list(iconsFolderPath)) {
            return icons
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        String fileNameWithoutExtension = fileName.split("\\.")[0];
                        return fileNameWithoutExtension.equals(iconName);
                    })
                    .findFirst()
                    .map(path -> path.toUri().toString())
                    .orElseThrow(() -> new RuntimeException("Icon not found by name: " + iconName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
