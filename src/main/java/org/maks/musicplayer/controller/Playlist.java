package org.maks.musicplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.model.SongPlayer;
import org.maks.musicplayer.model.Song;
import org.maks.musicplayer.utils.SongUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Playlist {

    @FXML
    private HBox musicListHBox;

    public void load(Widget widgetController) {
        List<Song> songList = SongUtils.musicList();

        for (int i = 0; i < songList.size(); i++) {
            Song song = songList.get(i);
            SongInfo songInfo = song.songInfo();

            final int songIndex = i;
            songInfo.setOnMouseClicked(_ ->
                widgetController.play(songIndex)
            );

            musicListHBox.getChildren().add(songInfo);
        }
    }

    public void add(Widget widgetController, String songFolderName) {
        File songFolder = new File("./songs/" + songFolderName);
        SongPlayer songPlayer = new SongPlayer(songFolder);
        SongInfo songInfo = new SongInfo();
        songInfo.load(songPlayer);

        int songIndex = calculateSongIndex(songFolderName);

        songInfo.setOnMouseClicked(_ ->
                widgetController.play(songIndex)
        );

        musicListHBox.getChildren().add(songIndex, songInfo);

        Song song = new Song(songInfo, songPlayer);
        SongUtils.musicList().add(songIndex, song);
    }

    private int calculateSongIndex(String targetSongFolderName) {
        File[] files = new File("./songs").listFiles();

        if (files == null) {
            throw new RuntimeException("Songs folder does not exist");
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (int i = 0; i < files.length; i++) {
            File songFolder = files[i];
            if (songFolder.getName().equals(targetSongFolderName)) {
                return i;
            }
        }

        return 0;
    }
}
