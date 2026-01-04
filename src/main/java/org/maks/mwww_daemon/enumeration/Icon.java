package org.maks.mwww_daemon.enumeration;

public enum Icon {
    LOADING("loading.gif"),
    NEW_SONG("new-song.png"),
    REPEAT_ON("repeat-on.png"),
    REPEAT_OFF("repeat-off.png"),
    SHUFFLE_ON("shuffle-on.png"),
    SHUFFLE_OFF("shuffle-off.png"),
    SPOTIFY("spotify.png");

    private final String iconName;
    Icon(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public String toString() {
        return iconName;
    }
}
