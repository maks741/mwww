package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.maks.musicplayer.enumeration.Icon;

import java.util.Arrays;

public class IconUtils {

    public static ImageView icon(Icon icon) {
        return new ImageView(iconPath(icon));
    }

    public static Image image(Icon icon) {
        return new Image(iconPath(icon));
    }

    private static String iconPath(Icon icon) {
        String iconName = icon.toString();

        return Arrays.stream(ResourceUtils.icons())
                .filter(file -> {
                    String fileNameWithoutExtension = file.getName().split("\\.")[0];
                    return fileNameWithoutExtension.equals(iconName);
                })
                .findFirst()
                .map(file -> file.toURI().toString())
                .orElseThrow(() -> new RuntimeException("Icon not found by name: " + iconName));
    }

}
