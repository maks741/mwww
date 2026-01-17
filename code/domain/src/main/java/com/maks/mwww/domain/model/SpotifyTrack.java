package com.maks.mwww.domain.model;

public class SpotifyTrack extends Track {

    private final String uri;

    public SpotifyTrack(String thumbnailUrl, String title, String uri) {
        super(thumbnailUrl, title);
        this.uri = uri;
    }

    public SpotifyTrack(String thumbnailUrl, String title) {
        super(thumbnailUrl, title);
        this.uri = "";
    }

    public String uri() {
        return uri;
    }
}
