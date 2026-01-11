package org.maks.mwww_daemon.backend.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.maks.mwww_daemon.shared.domain.enumeration.FXMLPath;

import java.io.IOException;

public class WidgetFXMLLoader<T> {

    private final FXMLLoader fxmlLoader;
    private final Parent parent;
    private final T controller;

    public WidgetFXMLLoader(FXMLPath fxmlPath) {
        this.fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath.toString()));
        this.parent = load();
        this.controller = fxmlLoader.getController();
    }

    private Parent load() {
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Parent parent() {
        return parent;
    }

    public T controller() {
        return controller;
    }
}
