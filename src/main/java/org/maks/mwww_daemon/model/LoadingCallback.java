package org.maks.mwww_daemon.model;

import java.util.function.Consumer;

public class LoadingCallback {

    private Consumer<Throwable> onCallback;
    private final String message;

    public LoadingCallback(String message) {
        this.message = message;
    }

    public void callback() {
        callback(null);
    }

    public void callback(Throwable e) {
        onCallback.accept(e);
    }

    public void setOnCallback(Consumer<Throwable> onCallback) {
        this.onCallback = onCallback;
    }

    public String message() {
        return message;
    }
}
