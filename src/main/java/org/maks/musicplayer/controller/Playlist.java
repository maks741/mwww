package org.maks.musicplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.utils.PlaylistUtils;

import java.util.List;

public class Playlist {

    @FXML
    private HBox musicListHBox;

    public void load(Widget widget) {
        PlaylistUtils playlistUtils = new PlaylistUtils();
        List<SongInfo> songInfoList = playlistUtils.songInfoList();

        for (int i = 0; i < songInfoList.size(); i++) {
            SongInfo songInfo = songInfoList.get(i);

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
        refresh(widget);
    }
}
