package com.maks.mwww.domain.enumeration;

public enum FXMLPath {
    WIDGET("/widget/widget.fxml");

    private final String path;

    FXMLPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }
}
