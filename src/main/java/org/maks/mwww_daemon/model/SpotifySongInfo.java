package org.maks.mwww_daemon.model;

import javafx.scene.image.Image;

public class SpotifySongInfo extends BaseSongInfo {

    private final String uri;

    public SpotifySongInfo(Image thumbnail, String title, String uri) {
        super(thumbnail, title);
        this.uri = uri;
    }

    public SpotifySongInfo(Image thumbnail, String title) {
        super(thumbnail, title);
        this.uri = "";
    }

    public String uri() {
        return uri;
    }
}
