package org.maks.mwww_daemon.shared.domain.enumeration;

public enum Icon {
    LOADING("loading.gif"),
    ADD("add.png"),
    X("x.png"),
    HEART("heart.png"),
    CHECKMARK("checkmark.png"),
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
