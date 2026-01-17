package com.maks.mwww.domain.model;

public class Track {

    private final String thumbnailUrl;
    private final String title;

    public Track(String thumbnailUrl, String title) {
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
    }

    public String thumbnailUrl() {
        return thumbnailUrl;
    }

    public String title() {
        return title;
    }
}
