package org.maks.musicplayer.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.maks.musicplayer.enumeration.FXMLPath;

import java.io.IOException;

public class WidgetFXMLLoader {

    private final FXMLLoader fxmlLoader;
    private final Parent parent;

    public WidgetFXMLLoader(FXMLPath fxmlPath) {
        fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath.toString()));
        parent = load();
    }

    public WidgetFXMLLoader(FXMLPath fxmlPath, Object controller) {
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setController(controller);
        fxmlLoader.setLocation(getClass().getResource(fxmlPath.toString()));
        parent = load();
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

    public FXMLLoader fxmlLoader() {
        return fxmlLoader;
    }
}
