package org.maks.musicplayer.model;

import javafx.scene.media.MediaPlayer;
import org.maks.musicplayer.utils.SongUtils;
import java.nio.file.Path;

public class SongPlayer {

    private final MediaPlayer mediaPlayer;

    public SongPlayer(Path songFolderPath) {
        SongUtils songUtils = new SongUtils();
        this.mediaPlayer = new MediaPlayer(songUtils.audio(songFolderPath));
    }

    public void play() {
        mediaPlayer.play();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void dispose() {
        mediaPlayer.stop();
        mediaPlayer.dispose();
    }
}
