package org.maks.mwww_daemon.service;

import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.maks.mwww_daemon.model.BaseSongInfo;
import org.maks.mwww_daemon.model.NotFoundSongInfo;

import java.util.function.Consumer;

public abstract class PlayerService<T extends BaseSongInfo> {

    private final Consumer<T> songUpdatedConsumer;

    protected Duration skipDuration = Duration.seconds(10);
    private boolean onRepeat = false;

    public PlayerService(Consumer<T> songUpdatedConsumer) {
        this.songUpdatedConsumer = songUpdatedConsumer;
    }

    protected void updateSongInfo(T songInfo) {
        songUpdatedConsumer.accept(songInfo);
        onSongUpdated(songInfo);
    }

    public void switchSong(String songName) {
        T songInfo = lookupSong(songName);

        if (songInfo instanceof NotFoundSongInfo) {
            return;
        }

        switchSong(songInfo);
    }

    protected void switchSong(T songInfo) {
        onPreSongChanged();
        updateSongInfo(songInfo);
    }

    protected void nextOrRepeat() {
        onPreSongChanged();

        if (onRepeat) {
            play();
        }

        next();
    }

    public void toggleOnRepeat() {
        onRepeat = !onRepeat;
    }

    public void setSkipDuration(int skipDuration) {
        this.skipDuration = Duration.seconds(skipDuration);
    }

    public abstract void loadFirstSong();

    protected abstract void onSongUpdated(T songInfo);

    protected abstract void onPreSongChanged();

    public abstract void play();

    public abstract void next();

    public abstract void previous();

    public abstract void skipForward();

    public abstract void skipBackward();

    public abstract void togglePause();

    protected abstract T lookupSong(String songId);

    public abstract void addSong(ImageView addIcon);

    public abstract void deleteSong();

    public abstract boolean isPlaying();

}
