package org.maks.musicplayer.model;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.maks.musicplayer.utils.SongUtils;

import java.io.File;

public class SongPlayer {

    private MediaPlayer mediaPlayer;
    private final SongInfoDto songInfoDto;

    public SongPlayer(File songFolder) {
        SongUtils songUtils = new SongUtils();
        SongDto songDto = songUtils.songDto(songFolder);
        this.mediaPlayer = new MediaPlayer(songDto.media());
        this.songInfoDto = songDto.songInfoDto();
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

    public SongInfoDto songInfoDto() {
        return songInfoDto;
    }

}
