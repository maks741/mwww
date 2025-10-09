package org.maks.mwww_daemon.model;

import javafx.scene.image.Image;

public class SongInfo {
    private final String songName;
    private final String songAuthor;
    private final Image songThumbnail;
    private final int songIndex;

    public SongInfo(String songName, String songAuthor, Image songThumbnail, int songIndex) {
        this.songName = songName;
        this.songIndex = songIndex;
        this.songThumbnail = songThumbnail;
        this.songAuthor = songAuthor;
    }

    public SongInfo(String message) {
        this(message, "unknown", null, -1);
    }

    public String songName() {
        return songName;
    }

    public String songAuthor() {
        return songAuthor;
    }

    public Image songThumbnail() {
        return songThumbnail;
    }

    public int songIndex() {
        return songIndex;
    }
}
