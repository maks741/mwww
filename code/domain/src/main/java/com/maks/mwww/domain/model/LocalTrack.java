package com.maks.mwww.domain.model;

public class LocalTrack extends Track {
    private final int trackIndex;

    public LocalTrack(String trackName, String artist, String thumbnailUrl, int trackIndex) {
        super(thumbnailUrl, trackName);

        this.trackIndex = trackIndex;
    }

    public LocalTrack(String message) {
        this(message, "unknown", null, -1);
    }

    public int trackIndex() {
        return trackIndex;
    }
}
