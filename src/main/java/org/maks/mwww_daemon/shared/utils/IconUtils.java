package org.maks.mwww_daemon.shared.utils;

import javafx.scene.image.Image;
import org.maks.mwww_daemon.shared.domain.enumeration.Icon;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class IconUtils {

    private static final Map<Icon, Image> cache = new HashMap<>();

    public static Image image(Icon icon) {
        if (cache.containsKey(icon)) {
            return cache.get(icon);
        }

        try (InputStream inputStream = IconUtils.class.getResourceAsStream("/icons/" + icon.toString())) {
            if (inputStream == null) {
                throw new RuntimeException("Icon not found: " + icon);
            }

            var image = new Image(inputStream);
            cache.put(icon, image);

            return image;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
