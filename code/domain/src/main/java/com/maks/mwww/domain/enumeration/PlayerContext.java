package com.maks.mwww.domain.enumeration;

public enum PlayerContext {
    LOCAL("local"),
    SPOTIFY("spotify");

    private final String context;

    PlayerContext(String context) {
        this.context = context;
    }

    public String context() {
        return context;
    }
}
