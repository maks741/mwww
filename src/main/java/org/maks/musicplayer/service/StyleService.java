package org.maks.musicplayer.service;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class StyleService {

    public void applyDefaultStyles(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/widget/style.css")).toExternalForm());
    }

    public void applyCustomStyles(Scene scene) {
        Path customCss = Paths.get(System.getProperty("user.home"), ".config", "mwww", "style.css");
        if (!Files.exists(customCss)) {
            return;
        }

        scene.getStylesheets().clear();
        scene.getStylesheets().add(customCss.toUri().toString());

        Pane root = (Pane) scene.getRoot();
        root.applyCss();

        double newWidth = root.prefWidth(-1);
        double newHeight = root.prefHeight(-1);

        Stage stage = (Stage) scene.getWindow();
        stage.setWidth(newWidth);
        stage.setHeight(newHeight);
    }
}
