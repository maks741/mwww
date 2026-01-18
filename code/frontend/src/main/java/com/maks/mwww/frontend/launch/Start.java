package com.maks.mwww.frontend.launch;

import com.maks.mwww.frontend.service.StyleService;
import com.maks.mwww.frontend.service.WidgetFXMLLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.maks.mwww.frontend.controller.Widget;
import com.maks.mwww.domain.enumeration.FXMLPath;
import com.maks.mwww.fifo.FifoCommandQueue;

public class Start extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("mwww-daemon");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setX(600);
        stage.setY(12);

        WidgetFXMLLoader<Widget> widgetFXMLLoader = new WidgetFXMLLoader<>(FXMLPath.WIDGET);
        Parent root = widgetFXMLLoader.parent();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        StyleService styleService = new StyleService(scene);
        styleService.applyDefaultStyles();
        FifoCommandQueue.instance().subscribe(styleService);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}