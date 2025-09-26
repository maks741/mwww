package org.maks.musicplayer.enumeration;

public enum FXMLPath {
    STATUS_BAR(basePath() + "statusbar/status-bar.fxml");

    private final String path;

    FXMLPath(String path) {
        this.path = path;
    }

    private static String basePath() {
        return "/org/maks/musicplayer/";
    }

    @Override
    public String toString() {
        return path;
    }
}
