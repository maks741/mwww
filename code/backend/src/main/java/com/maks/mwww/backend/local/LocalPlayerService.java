package com.maks.mwww.backend.local;

import com.maks.mwww.cqrs.bus.CommandBus;
import com.maks.mwww.cqrs.command.RequestInputCommand;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import com.maks.mwww.domain.model.LocalTrack;
import com.maks.mwww.backend.player.PlayerService;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class LocalPlayerService extends PlayerService<LocalTrack> {

    private static final Logger LOG = Logger.getLogger(LocalPlayerService.class.getName());

    private final LocalPlaylistUtils playlistUtils = new LocalPlaylistUtils();
    private final TrackIndexProvider indexProvider = new TrackIndexProvider();

    private MediaPlayer currentPlayer = null;
    private static final double INITIAL_VOLUME = 0.05;

    private boolean isTrackPlaying = false;
    private boolean onRepeat = false;

    public LocalPlayerService() {
        super(INITIAL_VOLUME);
    }

    @Override
    public void initialize() {
        LocalTrack track = playlistUtils.firstTrack();
        updateTrackInfo(track);
    }

    @Override
    protected void onTrackUpdated(LocalTrack track) {
        indexProvider.set(track.trackIndex());
    }

    @Override
    protected void onPreTrackChanged() {
        dispose();
    }

    @Override
    public void play() {
        if (currentPlayer != null) {
            return;
        }

        currentPlayer = playlistUtils.player(indexProvider.current());

        currentPlayer.setOnReady(() -> {
            // magic code, without it MediaPlayer makes a weird noise at the beginning of some tracks
            currentPlayer.seek(Duration.ZERO);

            currentPlayer.setVolume(volume);
            currentPlayer.setOnEndOfMedia(this::nextOrRepeat);

            currentPlayer.play();
            isTrackPlaying = true;
        });
    }

    @Override
    public void next() {
        switchTrack(indexProvider.next());
    }

    @Override
    public void previous() {
        switchTrack(indexProvider.previous());
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

        if (isTrackPlaying) {
            currentPlayer.pause();
        } else {
            currentPlayer.play();
        }

        isTrackPlaying = !isTrackPlaying;
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
    protected CompletableFuture<LocalTrack> lookupTrack(String query) {
        return CompletableFuture.completedFuture(playlistUtils.trackInfo(query));
    }

    @Override
    public CompletableFuture<Void> addTrack() {
        var completableFuture = new CompletableFuture<Void>();

        RequestInputCommand requestInputCommand = new RequestInputCommand(
                "Paste yt-dlp URL",
                url -> {
                    var downloadService = new YtDlpDownloadService();

                    CompletableFuture<String> task = downloadService.downloadTrack(url);
                    task.whenComplete((downloadedTrackName, ex) -> {
                        if (ex != null) {
                            LOG.severe(ex.getMessage());
                        } else {
                            switchTrack(downloadedTrackName);
                        }

                        completableFuture.complete(null);
                    });
                }
        );
        CommandBus.send(requestInputCommand);

        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> deleteTrack() {
        playlistUtils.deleteTrack(indexProvider.current());
        reloadCurrent();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void shutdown() {
        dispose();
    }

    private void nextOrRepeat() {
        onPreTrackChanged();

        if (onRepeat) {
            play();
        }

        next();
    }

    private LocalTrack lookupTrack(int trackIndex) {
        return playlistUtils.trackInfo(trackIndex);
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
        switchTrack(indexProvider.current());
    }

    private void switchTrack(int trackIndex) {
        switchTrack(lookupTrack(trackIndex));
    }

    private void dispose() {
        if (currentPlayer == null) {
            return;
        }

        currentPlayer.dispose();
        currentPlayer = null;
    }
}
