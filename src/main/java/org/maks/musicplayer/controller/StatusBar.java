package org.maks.musicplayer.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.maks.musicplayer.components.PauseToggle;
import org.maks.musicplayer.enumeration.FXMLPath;
import org.maks.musicplayer.service.WidgetFXMLLoader;

import java.net.URL;
import java.util.ResourceBundle;

public class StatusBar implements Initializable {

    private final Stage stage;
    private final Parent widget;
    private final Widget widgetController;

    @FXML
    private VBox body;

    @FXML
    private Label songName;

    @FXML
    private PauseToggle pauseToggle;

    public StatusBar(Stage stage) {
        this.stage = stage;

        WidgetFXMLLoader widgetFXMLLoader = new WidgetFXMLLoader(FXMLPath.WIDGET);

        widget = widgetFXMLLoader.parent();
        widgetController = widgetFXMLLoader.fxmlLoader().getController();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        body.getChildren().add(widget);
        pauseToggle.bind(widgetController.songPlayingProperty());
        bindMusicInfo();
        showHideWidget();

        widgetController.loadFirstSong();
    }

    private void bindMusicInfo() {
        widgetController.songPlayerProperty().addListener((
                _,
                _,
                songPlayer) -> {
            if (songPlayer == null) {
                return;
            }

            songName.setText(songPlayer.songInfoDto().songName());
        });
    }

    @FXML
    private void playOrPause() {
        widgetController.playPause();
    }

    @FXML
    private void previous() {
        widgetController.previous();
    }

    @FXML
    private void next() {
        widgetController.next();
    }

    @FXML
    private void showHideWidget() {
        double widgetHeight = widget.prefHeight(-1);
        double statusBarHeight = body.prefHeight(-1);
        double heightWithWidget = widgetHeight + statusBarHeight;

        if (body.getChildren().contains(widget)) {
            stage.setHeight(statusBarHeight);
            body.getChildren().remove(widget);
        } else {
            stage.setHeight(heightWithWidget);
            body.getChildren().add(widget);
        }

        Platform.runLater(() -> {
            stage.hide();
            stage.show();
        });
    }
}
