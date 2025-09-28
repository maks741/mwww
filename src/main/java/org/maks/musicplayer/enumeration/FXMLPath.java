package org.maks.musicplayer.enumeration;

public enum FXMLPath {
    WIDGET(basePath() + "widget/widget.fxml");

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
