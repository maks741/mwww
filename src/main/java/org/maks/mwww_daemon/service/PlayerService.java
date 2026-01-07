package org.maks.mwww_daemon.service;

import javafx.util.Duration;
import org.maks.mwww_daemon.components.AddIcon;
import org.maks.mwww_daemon.model.BaseSongInfo;

import java.util.function.Consumer;

public abstract class PlayerService<T extends BaseSongInfo> {

    private final Consumer<T> songUpdatedConsumer;

    protected Duration skipDuration = Duration.seconds(10);

    private static final double VOLUME_DELTA = 0.05;
    protected double volume;

    public PlayerService(Consumer<T> songUpdatedConsumer, double initialVolume) {
        this.songUpdatedConsumer = songUpdatedConsumer;
        this.volume = initialVolume;
    }

    protected void updateSongInfo(T songInfo) {
        songUpdatedConsumer.accept(songInfo);
        onSongUpdated(songInfo);
    }

    public void switchSong(String songName) {
        T songInfo = lookupSong(songName);

        if (songInfo == null) {
            return;
        }

        switchSong(songInfo);
    }

    protected void switchSong(T songInfo) {
        onPreSongChanged();
        updateSongInfo(songInfo);
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

    protected abstract void onSongUpdated(T songInfo);

    protected abstract void onPreSongChanged();

    public abstract void play();

    public abstract void next();

    public abstract void previous();

    public abstract void setVolume(double volume);

    public abstract void skipForward();

    public abstract void skipBackward();

    public abstract void togglePause();

    public abstract boolean toggleRepeat();

    public abstract boolean toggleShuffle();

    protected abstract T lookupSong(String songId);

    public abstract void addSong(AddIcon addIcon);

    public abstract void deleteSong(AddIcon addIcon);

    public abstract boolean isPlaying();

    public abstract void shutdown();

}
