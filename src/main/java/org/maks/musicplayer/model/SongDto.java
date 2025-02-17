package org.maks.musicplayer.model;

import javafx.scene.media.Media;

public record SongDto(
        SongInfoDto songInfoDto,
        Media media
) {
}
