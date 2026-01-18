module com.maks.mwww.cqrs {
    requires com.maks.mwww.domain;
    requires java.logging;

    exports com.maks.mwww.cqrs.api;
    exports com.maks.mwww.cqrs.bus;
    exports com.maks.mwww.cqrs.command;
}