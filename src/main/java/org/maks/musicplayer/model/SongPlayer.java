package org.maks.musicplayer.model;

import javafx.scene.media.MediaPlayer;
import org.maks.musicplayer.utils.SongUtils;

import java.io.File;

public class SongPlayer {

    private final MediaPlayer mediaPlayer;
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

    public void dispose() {
        mediaPlayer.stop();
        mediaPlayer.dispose();
    }

    public MediaPlayer mediaPlayer() {
        return mediaPlayer;
    }

    public SongInfoDto songInfoDto() {
        return songInfoDto;
    }

}
