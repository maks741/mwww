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

    private final SongIndexProvider indexProvider = new SongIndexProvider();

    private MediaPlayer currentPlayer = null;
    private static final double INITIAL_VOLUME = 0.05;

    private boolean isSongPlaying = false;
    private boolean onRepeat = false;

    public LocalPlayerService(Consumer<LocalSongInfo> songInfoConsumer) {
        super(songInfoConsumer, INITIAL_VOLUME);
    }

    @Override
    public void initialize() {
        LocalSongInfo song = lookupSong(indexProvider.first());
        updateSongInfo(song);
    }

    @Override
    protected void onSongUpdated(LocalSongInfo localSongInfo) {
        indexProvider.set(localSongInfo.songIndex());
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
    public void onSetSongCommand(String commandValue) {
        if (isPlaying()) {
            return;
        }

        switchSong(commandValue);
    }

    @Override
    public void play() {
        if (currentPlayer != null) {
            return;
        }

        var playlistUtils = new LocalPlaylistUtils();
        currentPlayer = playlistUtils.player(indexProvider.current());

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
        switchSong(indexProvider.next());
    }

    @Override
    public void previous() {
        switchSong(indexProvider.previous());
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
    public boolean toggleRepeat() {
        onRepeat = !onRepeat;
        return true;
    }

    @Override
    public boolean toggleShuffle() {
        indexProvider.toggleShuffle();
        return true;
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
        playlistUtils.deleteSong(indexProvider.current());
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
        switchSong(indexProvider.current());
    }

    private void switchSong(int songIndex) {
        switchSong(lookupSong(songIndex));
    }
}
