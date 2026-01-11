package org.maks.mwww_daemon.service.spotify;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.maks.mwww_daemon.components.AddIcon;
import org.maks.mwww_daemon.components.SearchField;
import org.maks.mwww_daemon.enumeration.PlayerctlStatus;
import org.maks.mwww_daemon.exception.CmdServiceException;
import org.maks.mwww_daemon.exception.PlayerctlNoTrackException;
import org.maks.mwww_daemon.model.LoadingCallback;
import org.maks.mwww_daemon.model.PlayerctlMetadata;
import org.maks.mwww_daemon.model.SpotifyTrack;
import org.maks.mwww_daemon.service.AsyncRunnerService;
import org.maks.mwww_daemon.service.BackendToUIBridge;
import org.maks.mwww_daemon.service.PlayerService;
import org.maks.mwww_daemon.service.spotify.client.SpotifyWebApiClient;
import org.maks.mwww_daemon.service.spotify.cmdoutputtransform.StringCmdOutputTransform;
import org.maks.mwww_daemon.utils.Config;
import org.maks.mwww_daemon.utils.ResourceUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class SpotifyPlayerService extends PlayerService<SpotifyTrack> {

    private static final Logger LOG = Logger.getLogger(SpotifyPlayerService.class.getName());

    private final PlayerctlMetadataService playerctlMetadataService = new PlayerctlMetadataService();
    private final PlayerctlStatusService playerctlStatusService = new PlayerctlStatusService();

    private final CmdService cmdService = new CmdService();
    private final SpotifydLifecycleService spotifydService = new SpotifydLifecycleService(INITIAL_VOLUME);

    private static final double INITIAL_VOLUME = 0.7;

    private String currentTrackUri;
    private PlayerctlStatus playerctlStatus = PlayerctlStatus.INACTIVE;

    public SpotifyPlayerService(BackendToUIBridge uiBridge) {
        super(uiBridge, INITIAL_VOLUME);
    }

    @Override
    public void initialize() {
        playerctlStatusService.listen(this::onPlayerctlStatusUpdated);

        if (playerctlInactive()) {
            spotifydService.restart();
        }

        playerctlMetadataService.listen(this::onPlayerctlMetadataUpdated);
    }

    @Override
    protected void onTrackUpdated(SpotifyTrack track) {
        currentTrackUri = track.uri();
    }

    @Override
    protected void onPreTrackChanged() {

    }

    @Override
    public void play() {
        Runnable play = () -> cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "play");

        if (playerctlInactive()) {
            recoverSpotifyd().whenComplete((_, ex) -> {
                if (ex == null) {
                    play.run();
                }
            });
        } else {
            play.run();
        }
    }

    @Override
    public void next() {
        if (playerctlInactive()) {
            return;
        }

        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "next");
    }

    @Override
    public void previous() {
        if (playerctlInactive()) {
            return;
        }

        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "previous");
    }

    @Override
    public void setVolume(double volume) {
        if (playerctlInactive()) {
            return;
        }

        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "volume", String.valueOf(volume));
    }

    @Override
    public void skipForward() {
        skip("+");
    }

    @Override
    public void skipBackward() {
        try {
            skip("-");
        } catch (CmdServiceException e) {
            if (e.cmdErrorMessage().contains("new position out of bounds")) {
                cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "position", "0");
            }
        }
    }

    @Override
    public void togglePause() {
        if (playerctlInactive()) {
            return;
        }

        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "play-pause");
    }

    @Override
    public boolean toggleRepeat() {
        try {
            playerctlToggle("loop", "Track", "Playlist");
        } catch (RuntimeException _) {
            return false;
        }

        return true;
    }

    @Override
    public boolean toggleShuffle() {
        try {
            playerctlToggle("shuffle", "On", "Off");
        } catch (RuntimeException _) {
            return false;
        }

        return true;
    }

    @Override
    protected CompletableFuture<SpotifyTrack> lookupTrack(String query) {
        var loadingCallback = new LoadingCallback("Searching...");
        uiBridge.requestLoading(loadingCallback);

        if (query.equals("me")) {
            // 'me' is a shortcut to open the home playlist
            String homePlaylistUri = "spotify:playlist:" + Config.spotifyPlaylistId();
            return openUri(homePlaylistUri, loadingCallback);
        }

        boolean isUri = query.startsWith("spotify:");
        if (isUri) {
            return openUri(query, loadingCallback);
        }

        return search(query, loadingCallback);
    }

    @Override
    public void addTrack(AddIcon addIcon, SearchField searchField) {
        var runner = new AsyncRunnerService();

        addIcon.loading();

        CompletableFuture<Void> task = runner.run(() -> {
            var apiClient = new SpotifyWebApiClient();
            apiClient.addTrackToPlaylist(currentTrackUri);
        });

        task.whenComplete((_, ex) -> {
            if (ex != null) {
                addIcon.fail();
                LOG.severe(ex.getMessage());
            } else {
                addIcon.like();
            }
        });
    }

    @Override
    public void deleteTrack(AddIcon addIcon) {
        var runner = new AsyncRunnerService();

        addIcon.loading();

        CompletableFuture<Void> task = runner.run(() -> {
            var apiClient = new SpotifyWebApiClient();
            apiClient.deleteTrackFromPlaylist(currentTrackUri);
        });

        task.whenComplete((_, ex) -> {
            if (ex != null) {
                addIcon.fail();
                LOG.severe(ex.getMessage());
            } else {
                addIcon.success();
            }
        });
    }

    @Override
    public void shutdown() {
        playerctlMetadataService.shutdown();
        playerctlStatusService.shutdown();

        if (playerctlInactive()) {
            return;
        }

        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "pause");
        setPlayerctlAttribute("shuffle", "Off");
        setPlayerctlAttribute("loop", "Playlist");
    }

    private void onPlayerctlStatusUpdated(String status) {
        PlayerctlStatus newStatus;
        if (status.isEmpty() || status.equals("Stopped")) {
            newStatus = PlayerctlStatus.INACTIVE;
        } else {
            newStatus = PlayerctlStatus.ACTIVE;
        }

        playerctlStatus = newStatus;
    }

    private void onPlayerctlMetadataUpdated(PlayerctlMetadata playerctlMetadata) {
        SpotifyTrack track = toSpotifyTrack(playerctlMetadata);
        Platform.runLater(() -> updateTrackInfo(track));
    }

    private SpotifyTrack toSpotifyTrack(PlayerctlMetadata playerctlMetadata) {
        String outputDir = ResourceUtils.cachePath(playerctlMetadata.trackId());
        String outputPathStr = outputDir + "/img.png";
        Path outputPath = Paths.get(outputPathStr);

        if (!Files.exists(outputPath)) {
            cmdService.runCmdCommand("mkdir", "-p", outputDir);
            cmdService.runCmdCommand(
                    "ffmpeg",
                    "-y",
                    "-i",
                    playerctlMetadata.artUrl(),
                    "-vf",
                    "crop=min(iw\\,ih):min(iw\\,ih),scale=400:400:flags=lanczos,format=rgba,geq=r='r(X,Y)':g='g(X,Y)':b='b(X,Y)':a='if(lte((X-200)^2+(Y-200)^2\\,200*200)\\,255\\,0)',scale=30:30:flags=lanczos",
                    "-update",
                    "true",
                    outputPathStr
            );
        }

        if (!Files.exists(outputPath)) {
            throw new RuntimeException("Error transforming thumbnail for " + playerctlMetadata.trackId() + ". Expected to find output file at path: " + outputPath);
        }

        Image thumbnail = new Image(outputPath.toUri().toString());

        Set<String> artistsSet = new HashSet<>(playerctlMetadata.artists());
        String title = String.join(", ", artistsSet) + " - " + playerctlMetadata.title();
        return new SpotifyTrack(thumbnail, title, playerctlMetadata.trackId());
    }

    private CompletableFuture<SpotifyTrack> openUri(String uri, LoadingCallback callback) {
        return open(CompletableFuture.completedFuture(uri), callback);
    }

    private CompletableFuture<SpotifyTrack> search(String query, LoadingCallback callback) {
        var runner = new AsyncRunnerService();

        CompletableFuture<String> searchFuture = runner.run(() -> {
            var client = new SpotifyWebApiClient();
            return client.search(query);
        });

        return open(searchFuture, callback);
    }

    private CompletableFuture<SpotifyTrack> open(CompletableFuture<String> uriFuture, LoadingCallback callback) {
        CompletableFuture<SpotifyTrack> trackFuture = new CompletableFuture<>();

        uriFuture.whenComplete((uri, ex) -> {
            if (uri == null || ex != null) {
                return;
            }

            cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "open", uri);

            PlayerctlMetadata playerctlMetadata;
            try {
                playerctlMetadata = playerctlMetadataService.readFullMetadata();
            } catch (PlayerctlNoTrackException e) {
                throw new RuntimeException(e);
            }
            SpotifyTrack track = toSpotifyTrack(playerctlMetadata);

            Platform.runLater(callback::callback);
            trackFuture.complete(track);
        });

        return trackFuture;
    }

    private void skip(String sign) {
        if (playerctlInactive()) {
            return;
        }

        String offsetPositon = ((int) skipDuration.toSeconds()) + sign;
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "position", offsetPositon);
    }

    private void playerctlToggle(String command, String on, String off) {
        if (playerctlInactive()) {
            throw new RuntimeException("Could not toggle playerctl " + command);
        }

        String currentStatus = cmdService.runCmdCommand(
                new StringCmdOutputTransform(),
                "playerctl",
                "-p",
                "spotifyd",
                command
        );

        String newStatus;
        if (currentStatus.equals(on)) {
            newStatus = off;
        } else {
            newStatus = on;
        }

        cmdService.runCmdCommand(
                new StringCmdOutputTransform(),
                "playerctl",
                "-p",
                "spotifyd",
                command,
                newStatus
        );
    }

    private void setPlayerctlAttribute(String attr, String value) {
        if (playerctlInactive()) {
            return;
        }

        cmdService.runCmdCommand(
                new StringCmdOutputTransform(),
                "playerctl",
                "-p",
                "spotifyd",
                attr,
                value
        );
    }

    private boolean playerctlInactive() {
        return playerctlStatus == PlayerctlStatus.INACTIVE;
    }

    private CompletableFuture<Void> recoverSpotifyd() {
        var loadingCallback = new LoadingCallback("Restarting spotifyd...");
        uiBridge.requestLoading(loadingCallback);

        var service = new AsyncRunnerService();

        return service.run(() -> {
            try {
                spotifydService.restart();
                playerctlMetadataService.restartTask();
                playerctlStatusService.restartTask();
                Platform.runLater(loadingCallback::callback);
            } catch (RuntimeException e) {
                Platform.runLater(() -> loadingCallback.callback(e));
                throw new RuntimeException(e);
            }
        });
    }
}
