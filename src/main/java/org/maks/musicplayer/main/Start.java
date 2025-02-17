package org.maks.musicplayer.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.maks.musicplayer.controller.StatusBar;
import org.maks.musicplayer.enumeration.FXMLPath;
import org.maks.musicplayer.service.WidgetFXMLLoader;

import java.io.IOException;

public class Start extends Application {
    @Override
    public void start(Stage owner) throws IOException {
        owner.setTitle("musicplayer");
        owner.initStyle(StageStyle.UTILITY);
        owner.setOpacity(0);
        owner.setWidth(0);
        owner.setHeight(0);
        owner.show();

        Stage gui = new Stage();
        gui.setTitle("musicplayer");
        gui.initOwner(owner);
        gui.initStyle(StageStyle.TRANSPARENT);
        gui.setX(600);
        gui.setY(12);

        StatusBar statusBar = new StatusBar(gui);
        WidgetFXMLLoader widgetFXMLLoader = new WidgetFXMLLoader(FXMLPath.STATUS_BAR, statusBar);
        Scene scene = new Scene(widgetFXMLLoader.parent());
        scene.setFill(Color.TRANSPARENT);

        gui.setAlwaysOnTop(false);
        gui.setScene(scene);
        gui.show();
    }

    public static void main(String[] args) {
        launch();
    }
}