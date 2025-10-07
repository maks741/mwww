package org.maks.mwww_daemon.enumeration;

public enum Icon {
    LOADING("loading.gif"),
    NEW_SONG("new-song.png"),
    REPEAT("repeat.png"),
    REPEAT_SINGLE("repeat-single.png");

    private final String iconName;
    Icon(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public String toString() {
        return iconName;
    }
}
