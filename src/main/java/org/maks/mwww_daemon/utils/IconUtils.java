package org.maks.mwww_daemon.utils;

import javafx.scene.image.Image;
import org.maks.mwww_daemon.enumeration.Icon;

import java.io.IOException;
import java.io.InputStream;

public class IconUtils {

    public Image image(Icon icon) {
        try (InputStream inputStream = getClass().getResourceAsStream("/icons/" + icon.toString())) {
            if (inputStream == null) {
                throw new RuntimeException("Icon not found: " + icon);
            }

            return new Image(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
