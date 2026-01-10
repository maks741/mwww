package org.maks.mwww_daemon.model;

import javafx.scene.image.Image;

public class LocalTrack extends Track {
    private final int trackIndex;

    public LocalTrack(String trackName, String artist, Image thumbnail, int trackIndex) {
        super(thumbnail, trackName);

        this.trackIndex = trackIndex;
    }

    public LocalTrack(String message) {
        this(message, "unknown", null, -1);
    }

    public int trackIndex() {
        return trackIndex;
    }
}
