package org.maks.musicplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import org.maks.musicplayer.components.MusicInfo;
import org.maks.musicplayer.model.MediaPlayerContainer;
import org.maks.musicplayer.model.Music;
import org.maks.musicplayer.utils.MusicUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MusicList {

    @FXML
    private HBox musicListHBox;

    public void load(Widget widgetController) {
        List<Music> musicList = MusicUtils.musicList();

        for (int i = 0; i < musicList.size(); i++) {
            Music music = musicList.get(i);
            MusicInfo musicInfo = music.musicInfo();

            final int songIndex = i;
            musicInfo.setOnMouseClicked(_ ->
                widgetController.play(songIndex)
            );

            musicListHBox.getChildren().add(musicInfo);
        }
    }

    public void add(Widget widgetController, String songFolderName) {
        File songFolder = new File("./songs/" + songFolderName);
        MediaPlayerContainer mediaPlayerContainer = new MediaPlayerContainer(songFolder);
        MusicInfo musicInfo = new MusicInfo();
        musicInfo.load(mediaPlayerContainer);

        int songIndex = calculateSongIndex(songFolderName);

        musicInfo.setOnMouseClicked(_ ->
                widgetController.play(songIndex)
        );

        musicListHBox.getChildren().add(songIndex, musicInfo);

        Music music = new Music(musicInfo, mediaPlayerContainer);
        MusicUtils.musicList().add(songIndex, music);
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
