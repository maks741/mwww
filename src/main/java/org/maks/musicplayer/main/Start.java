package org.maks.musicplayer.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.maks.musicplayer.enumeration.FXMLPath;
import org.maks.musicplayer.service.WidgetFXMLLoader;

import java.io.IOException;

public class Start extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("musicplayer");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setX(600);
        stage.setY(12);

        WidgetFXMLLoader widgetFXMLLoader = new WidgetFXMLLoader(FXMLPath.WIDGET);
        Scene scene = new Scene(widgetFXMLLoader.parent());
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}