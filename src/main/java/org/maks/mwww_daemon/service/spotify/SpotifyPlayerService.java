package org.maks.mwww_daemon.service.spotify;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.maks.mwww_daemon.components.AddIcon;
import org.maks.mwww_daemon.components.DynamicLabel;
import org.maks.mwww_daemon.exception.CmdServiceException;
import org.maks.mwww_daemon.exception.PlayerctlNoTrackException;
import org.maks.mwww_daemon.model.PlayerctlMetadata;
import org.maks.mwww_daemon.model.SpotifySongInfo;
import org.maks.mwww_daemon.service.AsyncRunnerService;
import org.maks.mwww_daemon.service.PlayerService;
import org.maks.mwww_daemon.service.spotify.client.SpotifyWebApiClient;
import org.maks.mwww_daemon.service.spotify.cmdoutputtransform.StringCmdOutputTransform;
import org.maks.mwww_daemon.utils.Config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SpotifyPlayerService extends PlayerService<SpotifySongInfo> {

    private static final Logger LOG = Logger.getLogger(SpotifyPlayerService.class.getName());

    private final PlayerctlMetadataService playerctlMetadataService = new PlayerctlMetadataService();
    private final CmdService cmdService = new CmdService();

    private static final double INITIAL_VOLUME = 0.7;

    private String currentTrackUri;

    public SpotifyPlayerService(Consumer<SpotifySongInfo> songInfoConsumer) {
        super(songInfoConsumer, INITIAL_VOLUME);
    }

    @Override
    public void initialize() {
        if (noPlayersFound()) {
            // register spotifyd in playerctl players
            String spotifydPid = cmdService.runCmdCommand(
                    new StringCmdOutputTransform(),
                    "pidof", "spotifyd"
            );
            cmdService.runCmdCommand(
                    "dbus-send",
                    "--print-reply",
                    "--dest=rs.spotifyd.instance" + spotifydPid,
                    "/rs/spotifyd/Controls",
                    "rs.spotifyd.Controls.TransferPlayback"
            );
        }

        // silently start playing the playlist, then seek to position 0 to make up for delay between playerctl open and playerctl pause
        setVolume(0);
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "open", Config.spotifyOpenOnStartupUri());
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "pause");
        setVolume(INITIAL_VOLUME);
        try {
            cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "position", "0");
        } catch (CmdServiceException e) {
            // this error may appear if nothing started playing (sometimes it does, sometimes not)
            if (!e.cmdErrorMessage().equals("Could not execute command: GDBus.Error:org.freedesktop.DBus.Error.Failed: can set position while nothing is playing")) {
                throw e;
            }
        }

        playerctlMetadataService.listen(this::onPlayerctlMetadataUpdated);
    }

    @Override
    protected void onSongUpdated(SpotifySongInfo songInfo) {
        currentTrackUri = songInfo.uri();
    }

    @Override
    protected void onPreSongChanged() {

    }

    @Override
    public void play() {
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "play");
    }

    @Override
    public void next() {
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "next");
    }

    @Override
    public void previous() {
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "previous");
    }

    @Override
    public void setVolume(double volume) {
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
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "play-pause");
    }

    @Override
    public boolean toggleRepeat() {
        try {
            playerctlToggle("loop", "Track", "Playlist");
        } catch (CmdServiceException _) {
            return false;
        }

        return true;
    }

    @Override
    public boolean toggleShuffle() {
        try {
            playerctlToggle("shuffle", "On", "Off");
        } catch (CmdServiceException _) {
            return false;
        }

        return true;
    }

    @Override
    protected SpotifySongInfo lookupSong(String query) {
        if (query.equals("me")) {
            // 'me' is a shortcut to open the home playlist
            query = "spotify:playlist:" + Config.spotifyPlaylistId();
        }

        boolean isUri = query.startsWith("spotify:");

        if (!isUri) {
            var client = new SpotifyWebApiClient();
            String searchResult = client.search(query);

            if (searchResult == null) {
                return null;
            }

            query = searchResult;
        }

        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "open", query);

        PlayerctlMetadata playerctlMetadata;
        try {
            playerctlMetadata = playerctlMetadataService.readFullMetadata();
        } catch (PlayerctlNoTrackException e) {
            throw new RuntimeException(e);
        }
        return toSpotifySongInfo(playerctlMetadata);
    }

    @Override
    public void addSong(AddIcon addIcon, DynamicLabel dynamicLabel) {
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
    public void deleteSong(AddIcon addIcon) {
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
    public boolean isPlaying() {
        return playerctlStatus().equals("Playing");
    }

    @Override
    public void shutdown() {
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "pause");
        setPlayerctlAttribute("shuffle", "Off");
        setPlayerctlAttribute("loop", "Playlist");
        playerctlMetadataService.shutdown();
    }

    private String playerctlStatus() {
        return cmdService.runCmdCommand(new StringCmdOutputTransform(), "playerctl", "-p", "spotifyd", "status");
    }

    private void onPlayerctlMetadataUpdated(PlayerctlMetadata playerctlMetadata) {
        SpotifySongInfo songInfo = toSpotifySongInfo(playerctlMetadata);
        Platform.runLater(() -> updateSongInfo(songInfo));
    }

    private SpotifySongInfo toSpotifySongInfo(PlayerctlMetadata playerctlMetadata) {
        String outputDir = Paths.get(System.getProperty("user.home"), ".cache", "mwww", playerctlMetadata.trackId()).toAbsolutePath().toString();
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
        return new SpotifySongInfo(thumbnail, title, playerctlMetadata.trackId());
    }

    private void skip(String sign) {
        String offsetPositon = ((int) skipDuration.toSeconds()) + sign;
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "position", offsetPositon);
    }

    private void playerctlToggle(String command, String on, String off) {
        if (noPlayersFound()) {
            return;
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
        if (noPlayersFound()) {
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

    private boolean noPlayersFound() {
        try {
            return playerctlStatus().equals("No players found");
        } catch (CmdServiceException e) {
            return e.cmdErrorMessage().equals("No players found");
        }
    }
}
