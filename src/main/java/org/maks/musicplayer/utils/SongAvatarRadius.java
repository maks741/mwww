package org.maks.musicplayer.utils;

public enum SongAvatarRadius {

    CIRCLE(180);

    private final int radius;

    SongAvatarRadius(int radius) {
        this.radius = radius;
    }

    public int value() {
        return radius;
    }
}
