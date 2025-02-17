package org.maks.musicplayer.model;

import javafx.scene.image.Image;

public record SongInfoDto(
        String songName,
        String songAuthor,
        Image songThumbnail
) {
}
