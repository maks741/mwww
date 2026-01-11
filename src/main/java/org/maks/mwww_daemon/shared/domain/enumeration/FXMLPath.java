package org.maks.mwww_daemon.shared.domain.enumeration;

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
