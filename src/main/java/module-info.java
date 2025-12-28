module org.maks.mwww_daemon {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires javafx.base;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    exports org.maks.mwww_daemon.main;
    opens org.maks.mwww_daemon.main to javafx.fxml;
    exports org.maks.mwww_daemon.components;
    opens org.maks.mwww_daemon.components to javafx.fxml;
    exports org.maks.mwww_daemon.controller;
    opens org.maks.mwww_daemon.controller to javafx.fxml;
    exports org.maks.mwww_daemon.utils;
    opens org.maks.mwww_daemon.utils to javafx.fxml, com.fasterxml.jackson.databind;
    exports org.maks.mwww_daemon.model;
    exports org.maks.mwww_daemon.enumeration;
    exports org.maks.mwww_daemon.service;
    opens org.maks.mwww_daemon.service to javafx.fxml;
    exports org.maks.mwww_daemon.fifo;
    opens org.maks.mwww_daemon.fifo to javafx.fxml;
    exports org.maks.mwww_daemon.service.local;
    opens org.maks.mwww_daemon.service.local to com.fasterxml.jackson.databind, javafx.fxml;
    exports org.maks.mwww_daemon.service.spotify;
    opens org.maks.mwww_daemon.service.spotify to javafx.fxml;
}