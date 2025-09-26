module org.maks.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires javafx.swing;
    requires javafx.base;
    requires javafx.graphics;


    exports org.maks.musicplayer.main;
    opens org.maks.musicplayer.main to javafx.fxml;
    exports org.maks.musicplayer.components;
    opens org.maks.musicplayer.components to javafx.fxml;
    exports org.maks.musicplayer.controller;
    opens org.maks.musicplayer.controller to javafx.fxml;
    exports org.maks.musicplayer.utils;
    opens org.maks.musicplayer.utils to javafx.fxml;
    exports org.maks.musicplayer.model;
    exports org.maks.musicplayer.enumeration;
    exports org.maks.musicplayer.service;
    opens org.maks.musicplayer.service to javafx.fxml;
}