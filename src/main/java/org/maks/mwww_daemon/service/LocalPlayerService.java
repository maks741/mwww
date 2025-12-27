package org.maks.mwww_daemon.service;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.maks.mwww_daemon.enumeration.Icon;
import org.maks.mwww_daemon.model.NotFoundSongInfo;
import org.maks.mwww_daemon.model.SongInfo;
import org.maks.mwww_daemon.model.TempSongInfo;
import org.maks.mwww_daemon.utils.IconUtils;
import org.maks.mwww_daemon.utils.PlaylistUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LocalPlayerService implements PlayerService {
    private int currentSongIndex = 0;
    private MediaPlayer currentPlayer = null;
    private boolean isSongPlaying = false;
    private boolean onRepeat = false;

    private Duration skipDuration = Duration.seconds(10);

    private Consumer<TempSongInfo> songUpdatedConsumer;

    @Override
    public void loadFirstSong() {
        SongInfo song = lookupSong(currentSongIndex);
        updateSongInfo(song);
    }

    private void updateSongInfo(SongInfo songInfo) {
        songUpdatedConsumer.accept(new TempSongInfo(songInfo.songThumbnail(), songInfo.songName()));
        currentSongIndex = songInfo.songIndex();
    }

    @Override
    public void addSong(ImageView addIcon) {
        // TODO: in spotify impl, you can use addIcon to turn it into a heart for some time and then back to plus
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
        PlaylistUtils playlistUtils = new PlaylistUtils();
        playlistUtils.deleteSong(currentSongIndex);
        reloadCurrent();
    }

    private SongInfo lookupSong(int songIndex) {
        PlaylistUtils playlistUtils = new PlaylistUtils();
        return playlistUtils.songInfo(songIndex);
    }

    private SongInfo lookupSong(String songName) {
        PlaylistUtils playlistUtils = new PlaylistUtils();
        return playlistUtils.songInfo(songName);
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
    public void play() {
        if (currentPlayer != null) {
            return;
        }

        PlaylistUtils playlistUtils = new PlaylistUtils();
        currentPlayer = playlistUtils.player(currentSongIndex);

        currentPlayer.setOnReady(() -> {
            // Magic code, without it MediaPlayer makes a weird noise at the beginning of some songs
            currentPlayer.seek(Duration.ZERO);

            currentPlayer.setVolume(0.05);
            currentPlayer.setOnEndOfMedia(this::skipToNextSong);

            currentPlayer.play();
            isSongPlaying = true;
        });
    }

    private void skipToNextSong() {
        dispose();

        if (!onRepeat) {
            next();
        }

        play();
    }

    private void reloadCurrent() {
        switchSong(currentSongIndex);
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
    public void skipForward() {
        skip(Duration::add);
    }

    @Override
    public void skipBackward() {
        skip(Duration::subtract);
    }

    private void skip(BiFunction<Duration, Duration, Duration> operation) {
        if (currentPlayer == null) {
            return;
        }

        Duration currentDuration = currentPlayer.getCurrentTime();
        Duration newDuration = operation.apply(currentDuration, skipDuration);
        currentPlayer.seek(newDuration);
    }

    @Override
    public void toggleOnRepeat() {
        onRepeat = !onRepeat;
    }

    private void switchSong(int songIndex) {
        switchSong(lookupSong(songIndex));
    }

    @Override
    public void switchSong(String songName) {
        SongInfo songInfo = lookupSong(songName);

        if (songInfo instanceof NotFoundSongInfo) {
            return;
        }

        switchSong(songInfo);
    }

    private void switchSong(SongInfo songInfo) {
        dispose();
        updateSongInfo(songInfo);
    }

    private void dispose() {
        if (currentPlayer == null) {
            return;
        }

        currentPlayer.dispose();
        currentPlayer = null;
    }

    @Override
    public void setOnSongUpdated(Consumer<TempSongInfo> consumer) {
        this.songUpdatedConsumer = consumer;
    }
    
    @Override
    public boolean isPlaying() {
        return isSongPlaying;
    }

    @Override
    public void setSkipDuration(int skipDuration) {
        this.skipDuration = Duration.seconds(skipDuration);
    }
}
