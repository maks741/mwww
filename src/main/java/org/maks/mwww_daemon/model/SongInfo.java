package org.maks.mwww_daemon.model;

import javafx.scene.image.Image;

public record SongInfo(
        String songName,
        String songAuthor,
        Image songThumbnail,
        int songIndex
) {
    public SongInfo(String message, int currentSongIndex) {
        this(message, "unknown", null, currentSongIndex);
    }
}
