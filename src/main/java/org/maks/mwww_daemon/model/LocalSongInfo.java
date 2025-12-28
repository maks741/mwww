package org.maks.mwww_daemon.model;

import javafx.scene.image.Image;

public class LocalSongInfo extends BaseSongInfo {
    private final String songName;
    private final String songAuthor;
    private final int songIndex;

    public LocalSongInfo(String songName, String songAuthor, Image thumbnail, int songIndex) {
        super(thumbnail, songName);

        this.songName = songName;
        this.songIndex = songIndex;
        this.songAuthor = songAuthor;
    }

    public LocalSongInfo(String message) {
        this(message, "unknown", null, -1);
    }

    public String songName() {
        return songName;
    }

    public String songAuthor() {
        return songAuthor;
    }

    public int songIndex() {
        return songIndex;
    }
}
