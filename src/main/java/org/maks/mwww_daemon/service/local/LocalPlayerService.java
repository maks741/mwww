package org.maks.mwww_daemon.service.local;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.maks.mwww_daemon.enumeration.Icon;
import org.maks.mwww_daemon.model.LocalSongInfo;
import org.maks.mwww_daemon.service.DownloadService;
import org.maks.mwww_daemon.service.PlayerService;
import org.maks.mwww_daemon.utils.IconUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LocalPlayerService extends PlayerService<LocalSongInfo> {
    private int currentSongIndex = 0;
    private MediaPlayer currentPlayer = null;
    private boolean isSongPlaying = false;
    private static final double INITIAL_VOLUME = 0.05;

    public LocalPlayerService(Consumer<LocalSongInfo> songInfoConsumer) {
        super(songInfoConsumer, INITIAL_VOLUME);
    }

    @Override
    public void initialize() {
        LocalSongInfo song = lookupSong(currentSongIndex);
        updateSongInfo(song);
    }

    @Override
    protected void onSongUpdated(LocalSongInfo localSongInfo) {
        currentSongIndex = localSongInfo.songIndex();
    }

    @Override
    protected void onPreSongChanged() {
        if (currentPlayer == null) {
            return;
        }

        currentPlayer.dispose();
        currentPlayer = null;
    }

    @Override
    public void play() {
        if (currentPlayer != null) {
            return;
        }

        var playlistUtils = new LocalPlaylistUtils();
        currentPlayer = playlistUtils.player(currentSongIndex);

        currentPlayer.setOnReady(() -> {
            // Magic code, without it MediaPlayer makes a weird noise at the beginning of some songs
            currentPlayer.seek(Duration.ZERO);

            currentPlayer.setVolume(volume);
            currentPlayer.setOnEndOfMedia(this::nextOrRepeat);

            currentPlayer.play();
            isSongPlaying = true;
        });
    }

    @Override
    public void next() {
        switchSong(++currentSongIndex);
    }

    @Override
    public void previous() {
        switchSong(--currentSongIndex);
    }

    @Override
    public void setVolume(double volume) {
        currentPlayer.setVolume(volume);
    }

    @Override
    public void skipForward() {
        skip(Duration::add);
    }

    @Override
    public void skipBackward() {
        skip(Duration::subtract);
    }

    @Override
    public void togglePause() {
        if (currentPlayer == null) {
            return;
        }

        if (isSongPlaying) {
            currentPlayer.pause();
        } else {
            currentPlayer.play();
        }

        isSongPlaying = !isSongPlaying;
    }

    @Override
    protected LocalSongInfo lookupSong(String songName) {
        var playlistUtils = new LocalPlaylistUtils();
        return playlistUtils.songInfo(songName);
    }

    @Override
    public void addSong(ImageView addIcon) {
        Image initialImage = addIcon.getImage();
        Image loadingGif = IconUtils.image(Icon.LOADING);
        addIcon.setImage(loadingGif);

        DownloadService downloadService = new DownloadService();
        CompletableFuture<String> task = downloadService.downloadSong();
        task.whenComplete((downloadedSongName, ex) -> {
            if (ex == null) {
                Platform.runLater(() -> switchSong(downloadedSongName));
            }

            addIcon.setImage(initialImage);
        });
    }

    @Override
    public void deleteSong() {
        var playlistUtils = new LocalPlaylistUtils();
        playlistUtils.deleteSong(currentSongIndex);
        reloadCurrent();
    }
    
    @Override
    public boolean isPlaying() {
        return isSongPlaying;
    }

    private void nextOrRepeat() {
        onPreSongChanged();

        if (onRepeat) {
            play();
        }

        next();
    }

    private LocalSongInfo lookupSong(int songIndex) {
        var playlistUtils = new LocalPlaylistUtils();
        return playlistUtils.songInfo(songIndex);
    }

    private void skip(BiFunction<Duration, Duration, Duration> operation) {
        if (currentPlayer == null) {
            return;
        }

        Duration currentDuration = currentPlayer.getCurrentTime();
        Duration newDuration = operation.apply(currentDuration, skipDuration);
        currentPlayer.seek(newDuration);
    }

    private void reloadCurrent() {
        switchSong(currentSongIndex);
    }

    private void switchSong(int songIndex) {
        switchSong(lookupSong(songIndex));
    }
}
