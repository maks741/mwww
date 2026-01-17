package org.maks.mwww_daemon.backend.service;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.maks.mwww_daemon.shared.domain.enumeration.FifoCommand;
import org.maks.mwww_daemon.backend.fifo.FifoCommandQueue;
import org.maks.mwww_daemon.backend.fifo.FifoCommandSubscriber;
import org.maks.mwww_daemon.shared.utils.ResourceUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class StyleService implements FifoCommandSubscriber {

    private final Scene scene;

    public StyleService(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void accept(FifoCommandQueue observable, FifoCommand command) {
        if (FifoCommand.RELOAD_STYLE == command) {
            applyCustomStyles();
        }
    }

    public void applyDefaultStyles() {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/widget/style.css")).toExternalForm());
    }

    private void applyCustomStyles() {
        Path customCss = ResourceUtils.customCssPath();
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
