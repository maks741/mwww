package org.maks.mwww_daemon.boot;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.maks.mwww_daemon.ui.Widget;
import org.maks.mwww_daemon.shared.domain.enumeration.FXMLPath;
import org.maks.mwww_daemon.backend.fifo.FifoCommandQueue;
import org.maks.mwww_daemon.backend.fifo.FifoService;
import org.maks.mwww_daemon.backend.service.StyleService;
import org.maks.mwww_daemon.backend.service.WidgetFXMLLoader;

public class Start extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("mwww-daemon");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setX(600);
        stage.setY(12);

        WidgetFXMLLoader<Widget> widgetFXMLLoader = new WidgetFXMLLoader<>(FXMLPath.WIDGET);
        Widget widget = widgetFXMLLoader.controller();
        Parent root = widgetFXMLLoader.parent();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        StyleService styleService = new StyleService(scene);
        styleService.applyDefaultStyles();

        stage.setScene(scene);
        stage.show();

        FifoCommandQueue fifoCommandQueue = new FifoCommandQueue();
        fifoCommandQueue.subscribe(widget);
        fifoCommandQueue.subscribe(styleService);

        new FifoService().read(fifoCommandQueue);
    }

    public static void main(String[] args) {
        launch(args);
    }
}