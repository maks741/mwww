package org.maks.musicplayer.model;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.maks.musicplayer.utils.SongUtils;

import java.io.File;

public class SongPlayer {

    private MediaPlayer mediaPlayer;
    private final String songName;
    private final String songAuthor;
    private final Image songThumbnail;

    public SongPlayer(File songFolder) {
        SongUtils songUtils = new SongUtils();
        SongDetails songDetails = songUtils.songDetails(songFolder);

        this.songName = songDetails.name();
        this.songAuthor = songDetails.author();
        this.songThumbnail = songDetails.thumbnail();
        this.mediaPlayer = new MediaPlayer(songDetails.media());
    }

    public void play() {
        mediaPlayer.play();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void refresh() {
        Media media = mediaPlayer.getMedia();

        mediaPlayer.stop();
        mediaPlayer.dispose();

        mediaPlayer = new MediaPlayer(media);
    }

    public MediaPlayer mediaPlayer() {
        return mediaPlayer;
    }

    public Image songAvatar() {
        return songThumbnail;
    }

    public String songName() {
        return songName;
    }

    public String songAuthor() {
        return songAuthor;
    }

}
