package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.maks.musicplayer.enumeration.IconName;

import java.io.File;

public class IconUtils {

    public static ImageView icon(IconName iconName) {
        return new ImageView(iconPath(iconName));
    }

    public static Image image(IconName iconName) {
        return new Image(iconPath(iconName));
    }

    private static String iconPath(IconName iconName) {
        String iconNameStr = iconName.toString();

        File iconFile = null;
        for (File icon : ResourceUtils.icons()) {
            if (icon.getName().startsWith(iconNameStr)) {
                String iconPath = ResourceUtils.iconsFolderPath() + "/" + icon.getName();
                iconFile = new File(iconPath);
            }
        }

        if (iconFile == null) {
            throw new RuntimeException("Icon not found by name: " + iconNameStr);
        }

        return iconFile.toURI().toString();
    }

}
