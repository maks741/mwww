package org.maks.mwww_daemon.service.local;

import javafx.application.Platform;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.maks.mwww_daemon.components.AddIcon;
import org.maks.mwww_daemon.components.DynamicLabel;
import org.maks.mwww_daemon.model.LocalSongInfo;
import org.maks.mwww_daemon.service.PlayerService;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class LocalPlayerService extends PlayerService<LocalSongInfo> {

    private static final Logger LOG = Logger.getLogger(LocalPlayerService.class.getName());

    private final LocalPlaylistUtils playlistUtils = new LocalPlaylistUtils();
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
        LocalSongInfo song = playlistUtils.firstSongInfo();
        updateSongInfo(song);
    }

    @Override
    protected void onSongUpdated(LocalSongInfo localSongInfo) {
        indexProvider.set(localSongInfo.songIndex());
    }

    @Override
    protected void onPreSongChanged() {
        dispose();
    }

    @Override
    public void play() {
        if (currentPlayer != null) {
            return;
        }

        currentPlayer = playlistUtils.player(indexProvider.current());

        currentPlayer.setOnReady(() -> {
            // magic code, without it MediaPlayer makes a weird noise at the beginning of some songs
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
        return playlistUtils.songInfo(songName);
    }

    @Override
    public void addSong(AddIcon addIcon, DynamicLabel dynamicLabel) {
        dynamicLabel.acceptNext("Paste yt-dlp URL").thenAccept(url -> {
            addIcon.loading();

            DownloadService downloadService = new DownloadService();

            CompletableFuture<String> task = downloadService.downloadSong(url);
            task.whenComplete((downloadedSongName, ex) -> {
                if (ex != null) {
                    addIcon.fail();
                    LOG.severe(ex.getMessage());
                } else {
                    addIcon.like();
                    Platform.runLater(() -> switchSong(downloadedSongName));
                }
            });
        });
    }

    @Override
    public void deleteSong(AddIcon addIcon) {
        playlistUtils.deleteSong(indexProvider.current());
        reloadCurrent();
    }
    
    @Override
    public boolean isPlaying() {
        return isSongPlaying;
    }

    @Override
    public void shutdown() {
        dispose();
    }

    private void nextOrRepeat() {
        onPreSongChanged();

        if (onRepeat) {
            play();
        }

        next();
    }

    private LocalSongInfo lookupSong(int songIndex) {
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

    private void dispose() {
        if (currentPlayer == null) {
            return;
        }

        currentPlayer.dispose();
        currentPlayer = null;
    }
}
