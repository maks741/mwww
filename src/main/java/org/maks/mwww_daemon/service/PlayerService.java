package org.maks.mwww_daemon.service;

import javafx.application.Platform;
import javafx.util.Duration;
import org.maks.mwww_daemon.components.AddIcon;
import org.maks.mwww_daemon.components.SearchField;
import org.maks.mwww_daemon.model.Track;

import java.util.concurrent.CompletableFuture;

public abstract class PlayerService<T extends Track> {

    protected final BackendToUIBridge uiBridge;

    protected Duration skipDuration = Duration.seconds(10);

    private static final double VOLUME_DELTA = 0.05;
    protected double volume;

    public PlayerService(BackendToUIBridge uiBridge, double initialVolume) {
        this.uiBridge = uiBridge;
        this.volume = initialVolume;
    }

    protected void updateTrackInfo(T track) {
        uiBridge.updateTrack(track);
        onTrackUpdated(track);
    }

    public void switchTrack(String query) {
        CompletableFuture<T> trackFuture = lookupTrack(query);

        trackFuture.whenComplete((track, ex) -> {
            if (track == null || ex != null) {
                return;
            }

            Platform.runLater(() -> switchTrack(track));
        });
    }

    protected void switchTrack(T track) {
        onPreTrackChanged();
        updateTrackInfo(track);
    }

    public void volumeUp() {
        volume += VOLUME_DELTA;
        setVolume(volume);
    }

    public void volumeDown() {
        volume -= VOLUME_DELTA;
        setVolume(volume);
    }

    public abstract void initialize();

    protected abstract void onTrackUpdated(T track);

    protected abstract void onPreTrackChanged();

    public abstract void play();

    public abstract void next();

    public abstract void previous();

    public abstract void setVolume(double volume);

    public abstract void skipForward();

    public abstract void skipBackward();

    public abstract void togglePause();

    public abstract boolean toggleRepeat();

    public abstract boolean toggleShuffle();

    protected abstract CompletableFuture<T> lookupTrack(String query);

    public abstract void addTrack(AddIcon addIcon, SearchField searchField);

    public abstract void deleteTrack(AddIcon addIcon);

    public abstract void shutdown();

}
