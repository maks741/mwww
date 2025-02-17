package org.maks.musicplayer.enumeration;

public enum Icon {
    LOADING("loading"),
    NEW_SONG("new-song"),
    NEXT("next"),
    PAUSE("pause"),
    PLAY("play"),
    PREVIOUS("previous"),
    REPEAT("repeat"),
    REPEAT_SINGLE("repeat-single"),
    SHUTDOWN("shutdown"),
    STATUS_BAR("status-bar");

    private final String iconName;
    Icon(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public String toString() {
        return iconName;
    }
}
