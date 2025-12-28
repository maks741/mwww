package org.maks.mwww_daemon.model;

import javafx.scene.image.Image;

public class BaseSongInfo {

    private final Image thumbnail;
    private final String title;

    public BaseSongInfo(Image thumbnail, String title) {
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
