package org.maks.mwww_daemon.backend.service;

import org.maks.mwww_daemon.shared.domain.model.LoadingCallback;
import org.maks.mwww_daemon.shared.domain.model.Track;

import java.util.function.Consumer;

public class BackendToUIBridge {

    private Consumer<Track> trackUpdatedConsumer = _ -> {};
    private Consumer<LoadingCallback> loadingCallbackConsumer = _ -> {};

    public void setOnTrackUpdated(Consumer<Track> trackUpdatedConsumer) {
        this.trackUpdatedConsumer = trackUpdatedConsumer;
    }

    public void updateTrack(Track track) {
        this.trackUpdatedConsumer.accept(track);
    }

    public void setOnRequestLoading(Consumer<LoadingCallback> loadingCallbackConsumer) {
        this.loadingCallbackConsumer = loadingCallbackConsumer;
    }

    public void requestLoading(LoadingCallback loadingCallback) {
        this.loadingCallbackConsumer.accept(loadingCallback);
    }

}
