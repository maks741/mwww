package org.maks.musicplayer.model;

import javafx.scene.image.Image;

public record SongInfo(
        String songName,
        String songAuthor,
        Image songThumbnail
) {
}
