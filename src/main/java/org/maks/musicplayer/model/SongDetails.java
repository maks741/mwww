package org.maks.musicplayer.model;

import javafx.scene.image.Image;
import javafx.scene.media.Media;

public record SongDetails(
        String name,
        String author,
        Image thumbnail,
        Media media
) {
}
