package org.maks.musicplayer.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.maks.musicplayer.components.RepeatSongToggle;
import org.maks.musicplayer.enumeration.Icon;
import org.maks.musicplayer.exception.SongDirectoryEmptyException;
import org.maks.musicplayer.model.SongInfo;
import org.maks.musicplayer.service.DownloadService;
import org.maks.musicplayer.utils.IconUtils;
import org.maks.musicplayer.utils.PlaylistUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class StatusBar implements Initializable {

    @FXML
    private VBox body;

    @FXML
    private ImageView statusBarIcon;

    @FXML
    private Label songName;

    @FXML
    private RepeatSongToggle repeatSongToggle;

    @FXML
    private ImageView addIcon;

    private int currentSongIndex = 0;
    private MediaPlayer currentPlayer = null;
    private boolean isSongPlaying = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadFirstSong();
        addKeybindings();
    }

    private void loadFirstSong() {
        SongInfo song = songByIndex(currentSongIndex);
        updateSongInfo(song);
    }

    private void updateSongInfo(SongInfo songInfo) {
        statusBarIcon.setImage(songInfo.songThumbnail());
        songName.setText(songInfo.songName());
    }

    private void addKeybindings() {
        KeyCombination exit = new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN);
        KeyCombination next = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
        KeyCombination previous = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
        KeyCombination newSong = new KeyCodeCombination(KeyCode.PLUS, KeyCombination.META_DOWN);

        body.sceneProperty().addListener((_, _, scene) -> {
            scene.setOnKeyPressed(keyEvent -> {
                if (next.match(keyEvent)) {
                    next();
                } else if (previous.match(keyEvent)) {
                    previous();
                } else if (exit.match(keyEvent)) {
                    shutdown();
                } else if (newSong.match(keyEvent)) {
                    addSong();
                } else {
                    switch (keyEvent.getCode()) {
                        case SPACE -> playPause();
                        case R -> repeatSongToggle.toggleOnRepeat();
                    }
                }
            });

            scene.setOnKeyReleased(keyEvent -> {
                if (KeyCode.ALT == keyEvent.getCode()) {
                    play();
                }
            });
        });
    }

    private void addSong() {
        Image initialImage = addIcon.getImage();
        Image loadingGif = IconUtils.image(Icon.LOADING);
        addIcon.setImage(loadingGif);

        DownloadService downloadService = new DownloadService();
        downloadService.downloadSong().setOnSucceeded(_ ->
                Platform.runLater(() ->
                        addIcon.setImage(initialImage)
                )
        );
    }

    private SongInfo songByIndex(int songIndex) throws SongDirectoryEmptyException {
        PlaylistUtils playlistUtils = new PlaylistUtils();
        return playlistUtils.songInfo(songIndex);
    }

    private void playPause() {
        if (isSongPlaying) {
            pause();
        } else {
            unpause();
        }
    }

    private void play() {
        if (currentPlayer != null) {
            return;
        }

        PlaylistUtils playlistUtils = new PlaylistUtils();
        currentPlayer = playlistUtils.player(currentSongIndex);

        currentPlayer.setOnReady(() -> {
            // Magic code, without it MediaPlayer makes a weird noise at the beginning of some songs
            currentPlayer.seek(Duration.ZERO);

            currentPlayer.setVolume(0.05);
            currentPlayer.setOnEndOfMedia(this::skipToNextSong);

            currentPlayer.play();
            isSongPlaying = true;
        });
    }

    private void unpause() {
        if (currentPlayer == null) {
            return;
        }

        currentPlayer.play();
        isSongPlaying = true;
    }

    private void pause() {
        if (currentPlayer == null) {
            return;
        }

        currentPlayer.pause();
        isSongPlaying = false;
    }

    private void skipToNextSong() {
        dispose();

        if (repeatSongToggle.onRepeat()) {
            play();
        } else {
            next();
        }
    }

    private void next() {
        switchSong(++currentSongIndex);
    }

    private void previous() {
        switchSong(--currentSongIndex);
    }

    private void switchSong(int songIndex) {
        dispose();
        SongInfo song = songByIndex(songIndex);
        updateSongInfo(song);
    }

    private void dispose() {
        if (currentPlayer == null) {
            return;
        }

        currentPlayer.dispose();
        currentPlayer = null;
    }

    private void shutdown() {
        System.exit(0);
    }
}
