module com.maks.mwww.frontend {
    requires com.maks.mwww.backend;
    requires com.maks.mwww.domain;
    requires com.maks.mwww.fifo;
    requires com.maks.mwww.cqrs;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens com.maks.mwww.frontend.components to javafx.fxml;
    opens com.maks.mwww.frontend.controller to javafx.fxml;
    exports com.maks.mwww.frontend.launch;
}