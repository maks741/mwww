package org.maks.musicplayer.model;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.nio.file.Path;

public class SongPlayer {

    private final MediaPlayer mediaPlayer;

    public SongPlayer(Path songDirPath) {
        Path songMediaFile = songDirPath.resolve("media.wav");
        Media media = new Media(songMediaFile.toUri().toString());
        this.mediaPlayer = new MediaPlayer(media);
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

    public MediaPlayer mediaPlayer() {
        return mediaPlayer;
    }
}
