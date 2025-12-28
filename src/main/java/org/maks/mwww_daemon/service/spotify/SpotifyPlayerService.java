package org.maks.mwww_daemon.service.spotify;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.maks.mwww_daemon.model.BaseSongInfo;
import org.maks.mwww_daemon.model.PlayerctlMetadata;
import org.maks.mwww_daemon.service.PlayerService;

import java.util.List;
import java.util.function.Consumer;

public class SpotifyPlayerService extends PlayerService<BaseSongInfo> {

    private final CmdService cmdService = new CmdService();

    private boolean playlistLoaded = false;

    public SpotifyPlayerService(Consumer<BaseSongInfo> songInfoConsumer) {
        super(songInfoConsumer);
    }

    @Override
    public void initialize() {
        new PlayerctlMetadataService(this::onPlayerctlMetadataUpdated);
        // currentSongUri = likedSongUris.getFirst();
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
        List<String> statusOutput = cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "status");
        String statusOutputStr = statusOutput.getFirst();

        return statusOutputStr.equals("Playing");
    }

    @Override
    public void shutdown() {
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "pause");
    }

    private void onPlayerctlMetadataUpdated(PlayerctlMetadata playerctlMetadata) {
        Image thumbnail = new Image(playerctlMetadata.artUrl());
        String title = String.join(", ", playerctlMetadata.artists()) + " - " + playerctlMetadata.title();
        var songInfo = new BaseSongInfo(thumbnail, title);
        Platform.runLater(() -> updateSongInfo(songInfo));
    }

    private void skip(String sign) {
        String offsetPositon = ((int) skipDuration.toSeconds()) + sign;
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "position", offsetPositon);
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
