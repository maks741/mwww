package org.maks.mwww_daemon.model;

import javafx.scene.image.Image;

public class Track {

    private final Image thumbnail;
    private final String title;

    public Track(Image thumbnail, String title) {
        this.thumbnail = thumbnail;
        this.title = title;
    }

    public Image thumbnail() {
        return thumbnail;
    }

    public String title() {
        return title;
    }
}
