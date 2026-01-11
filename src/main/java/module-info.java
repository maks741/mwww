module org.maks.mwww_daemon {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires javafx.base;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires java.net.http;
    requires jdk.httpserver;
    requires com.google.gson;

    // backend
    exports org.maks.mwww_daemon.backend.cmd;
    opens org.maks.mwww_daemon.backend.cmd to javafx.fxml;
    exports org.maks.mwww_daemon.backend.fifo;
    opens org.maks.mwww_daemon.backend.fifo to javafx.fxml;
    exports org.maks.mwww_daemon.backend.local;
    opens org.maks.mwww_daemon.backend.local to com.fasterxml.jackson.databind, javafx.fxml;
    exports org.maks.mwww_daemon.backend.service;
    opens org.maks.mwww_daemon.backend.service to javafx.fxml;
    exports org.maks.mwww_daemon.backend.spotify;
    opens org.maks.mwww_daemon.backend.spotify to javafx.fxml;

    // boot
    exports org.maks.mwww_daemon.boot;
    opens org.maks.mwww_daemon.boot to javafx.fxml;

    // shared
    exports org.maks.mwww_daemon.shared.domain.dto;
    opens org.maks.mwww_daemon.shared.domain.dto to javafx.fxml;
    exports org.maks.mwww_daemon.shared.domain.exception;
    exports org.maks.mwww_daemon.shared.domain.model;
    exports org.maks.mwww_daemon.shared.domain.enumeration;
    exports org.maks.mwww_daemon.shared.utils;
    opens org.maks.mwww_daemon.shared.utils to javafx.fxml, com.fasterxml.jackson.databind;

    // ui
    exports org.maks.mwww_daemon.ui;
    opens org.maks.mwww_daemon.ui to javafx.fxml;
    exports org.maks.mwww_daemon.ui.components;
    opens org.maks.mwww_daemon.ui.components to javafx.fxml;
}