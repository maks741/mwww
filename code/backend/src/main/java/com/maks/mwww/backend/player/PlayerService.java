package com.maks.mwww.backend.player;

import com.maks.mwww.cqrs.bus.CommandBus;
import com.maks.mwww.cqrs.command.SearchTrackCommand;
import com.maks.mwww.cqrs.command.UpdateTrackCommand;
import javafx.application.Platform;
import javafx.util.Duration;
import com.maks.mwww.domain.model.Track;

import java.util.concurrent.CompletableFuture;

public abstract class PlayerService<T extends Track> {

    protected Duration skipDuration = Duration.seconds(10);

    private static final double VOLUME_DELTA = 0.05;
    protected double volume;

    public PlayerService(double initialVolume) {
        this.volume = initialVolume;
        CommandBus.subscribe(SearchTrackCommand.class, command -> this.switchTrack(command.query()));
    }

    protected void updateTrackInfo(T track) {
        CommandBus.send(new UpdateTrackCommand(track));
        onTrackUpdated(track);
    }

    protected void switchTrack(String query) {
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

    public abstract CompletableFuture<Void> addTrack();

    public abstract CompletableFuture<Void> deleteTrack();

    public abstract void shutdown();

}
