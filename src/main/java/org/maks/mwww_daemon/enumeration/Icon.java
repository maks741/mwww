package org.maks.mwww_daemon.enumeration;

public enum Icon {
    LOADING("loading"),
    NEW_SONG("new-song"),
    REPEAT("repeat"),
    REPEAT_SINGLE("repeat-single");

    private final String iconName;
    Icon(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public String toString() {
        return iconName;
    }
}
