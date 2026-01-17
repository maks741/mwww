package com.maks.mwww.cqrs;

import com.maks.mwww.domain.model.LoadingCallback;
import com.maks.mwww.domain.model.Track;

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
