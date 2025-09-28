package org.maks.musicplayer.main;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.maks.musicplayer.controller.Widget;
import org.maks.musicplayer.enumeration.FXMLPath;
import org.maks.musicplayer.service.FifoService;
import org.maks.musicplayer.service.StyleService;
import org.maks.musicplayer.service.WidgetFXMLLoader;

import java.io.IOException;

public class Start extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("musicplayer");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setX(600);
        stage.setY(12);

        WidgetFXMLLoader<Widget> widgetFXMLLoader = new WidgetFXMLLoader<>(FXMLPath.WIDGET);
        Widget widget = widgetFXMLLoader.controller();
        Parent root = widgetFXMLLoader.parent();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        StyleService styleService = new StyleService();
        styleService.applyDefaultStyles(scene);

        stage.setScene(scene);
        stage.show();

        new FifoService().read(
                () -> styleService.applyCustomStyles(scene),
                widget::switchSong
        );
    }

    public static void main(String[] args) {
        launch();
    }
}