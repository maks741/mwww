package org.maks.musicplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.model.SongPlayer;
import org.maks.musicplayer.model.Song;
import org.maks.musicplayer.utils.PlaylistUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Playlist {

    @FXML
    private HBox musicListHBox;

    public void load(Widget widget) {
        List<Song> songList = PlaylistUtils.playlist();

        for (int i = 0; i < songList.size(); i++) {
            Song song = songList.get(i);
            SongInfo songInfo = song.songInfo();

            final int songIndex = i;
            songInfo.setOnMouseClicked(_ ->
                widget.play(songIndex)
            );

            musicListHBox.getChildren().add(songInfo);
        }
    }

    private void refresh(Widget widget) {
        musicListHBox.getChildren().clear();
        load(widget);
    }

    public void add(Widget widget) {
        PlaylistUtils.refresh();
        refresh(widget);
    }
}
