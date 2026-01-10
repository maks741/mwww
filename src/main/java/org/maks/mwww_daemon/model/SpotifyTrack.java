package org.maks.mwww_daemon.model;

import javafx.scene.image.Image;

public class SpotifyTrack extends Track {

    private final String uri;

    public SpotifyTrack(Image thumbnail, String title, String uri) {
        super(thumbnail, title);
        this.uri = uri;
    }

    public SpotifyTrack(Image thumbnail, String title) {
        super(thumbnail, title);
        this.uri = "";
    }

    public String uri() {
        return uri;
    }
}
