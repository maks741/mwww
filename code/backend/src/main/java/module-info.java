module com.maks.mwww.backend {
    requires com.maks.mwww.domain;
    requires com.maks.mwww.cqrs;
    requires java.logging;
    requires java.net.http;
    requires jdk.httpserver;
    requires com.google.gson;

    requires javafx.base;
    requires javafx.media;

    exports com.maks.mwww.backend.player;
}