package org.maks.mwww_daemon.service.spotify;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.maks.mwww_daemon.enumeration.Icon;
import org.maks.mwww_daemon.exception.CmdServiceException;
import org.maks.mwww_daemon.model.BaseSongInfo;
import org.maks.mwww_daemon.model.PlayerctlMetadata;
import org.maks.mwww_daemon.service.PlayerService;
import org.maks.mwww_daemon.service.spotify.cmdoutputtransform.StringCmdOutputTransform;
import org.maks.mwww_daemon.utils.IconUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public class SpotifyPlayerService extends PlayerService<BaseSongInfo> {

    private final CmdService cmdService = new CmdService();

    private static final double INITIAL_VOLUME = 0.7;
    private boolean playlistLoaded = false;

    public SpotifyPlayerService(Consumer<BaseSongInfo> songInfoConsumer) {
        super(songInfoConsumer, INITIAL_VOLUME);
    }

    @Override
    public void initialize() {
        this.playlistLoaded = isPlaylistLoaded();

        if (!playlistLoaded) {
            updateSongInfo(new BaseSongInfo(IconUtils.image(Icon.SPOTIFY), "Release Ctrl to start playing"));
        }

        new PlayerctlMetadataService(this::onPlayerctlMetadataUpdated);
    }

    @Override
    protected void onSongUpdated(BaseSongInfo songInfo) {

    }

    @Override
    protected void onPreSongChanged() {

    }

    @Override
    public void play() {
        if (!playlistLoaded) {
            cmdService.runCmdCommand(
                    "spotatui",
                    "play",
                    "--uri",
                    "spotify:playlist:4PKRumbJb3aUG4RVLDw7ax",
                    "--playlist",
                    "--device",
                    "ArchLinux"
            );
            setVolume(INITIAL_VOLUME);
            playlistLoaded = true;
        } else {
            cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "play");
        }
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
        skip("-");
    }

    @Override
    public void togglePause() {
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "play-pause");
    }

    @Override
    protected BaseSongInfo lookupSong(String songId) {
        return null;
    }

    @Override
    public void addSong(ImageView addIcon) {
        // TODO: You can use addIcon to turn it into a heart for some time and then back to plus
        cmdService.runCmdCommand("spotatui", "playback", "--like");
    }

    @Override
    public void deleteSong() {
        // TODO: You can use addIcon to turn it into a heart for some time and then back to plus
        cmdService.runCmdCommand("spotatui", "playback", "--dislike");
    }

    @Override
    public void onSetSongCommand(String commandValue) {
        // TODO
    }

    @Override
    public boolean isPlaying() {
        return playerctlStatus().equals("Playing");
    }

    @Override
    public void shutdown() {
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "pause");
    }

    private String playerctlStatus() {
        return cmdService.runCmdCommand(new StringCmdOutputTransform(), "playerctl", "-p", "spotifyd", "status");
    }

    private void onPlayerctlMetadataUpdated(PlayerctlMetadata playerctlMetadata) {
        String outputDir = "/home/maks/.cache/mwww/" + playerctlMetadata.trackId();
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

        String title = String.join(", ", playerctlMetadata.artists()) + " - " + playerctlMetadata.title();
        var songInfo = new BaseSongInfo(thumbnail, title);
        Platform.runLater(() -> updateSongInfo(songInfo));
    }

    private void skip(String sign) {
        String offsetPositon = ((int) skipDuration.toSeconds()) + sign;
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "position", offsetPositon);
    }

    private boolean isPlaylistLoaded() {
        try {
            return !playerctlStatus().equals("No players found");
        } catch (CmdServiceException e) {
            return false;
        }
    }

    private List<String> listLikedSongUris() {
        List<String> likedSongsOutput = cmdService.runCmdCommand("spotatui", "list",  "--liked");
        return likedSongsOutput.stream()
                .filter(s -> s.contains("spotify:track:"))
                .map(s -> {
                    int startIndex = s.lastIndexOf('(') + 1;
                    int endIndex = s.lastIndexOf(')');
                    return s.substring(startIndex, endIndex);
                }).toList();
    }
}
