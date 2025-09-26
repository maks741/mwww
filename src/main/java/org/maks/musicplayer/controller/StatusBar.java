package org.maks.musicplayer.controller;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import org.maks.musicplayer.model.SongPlayer;
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
    private boolean isSongPlaying = false;
    private final ObjectProperty<SongPlayer> songPlayerProperty = new SimpleObjectProperty<>();

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

        body.sceneProperty().addListener((v, o, scene) -> {
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
        downloadService.downloadSong().setOnSucceeded(event ->
                Platform.runLater(() ->
                        addIcon.setImage(initialImage)
                )
        );
    }

    private SongInfo songByIndex(int songIndex) throws SongDirectoryEmptyException {
        PlaylistUtils playlistUtils = new PlaylistUtils();
        int amountOfMusic = playlistUtils.amountOfSongs();

        if (amountOfMusic == 0) {
            throw new SongDirectoryEmptyException("Songs directory is empty");
        }

        int index = songIndex % amountOfMusic;

        if (index < 0) {
            index = amountOfMusic + index;
        }

        return playlistUtils.songInfo(index);
    }

    private void playPause() {
        if (isSongPlaying) {
            pause();
        } else {
            play();
        }
    }

    public void play() {
        if (songPlayerProperty.get() == null) {
            SongPlayer songPlayer = new PlaylistUtils().songPlayer(currentSongIndex);
            songPlayerProperty.set(songPlayer);
        }

        SongPlayer songPlayer = songPlayerProperty.get();
        MediaPlayer mediaPlayer = songPlayer.mediaPlayer();

        boolean mediaNotPlayerReady = mediaPlayer.getCycleDuration() == Duration.UNKNOWN;
        Runnable play = () -> play(mediaPlayer, songPlayer, mediaNotPlayerReady);
        if (mediaNotPlayerReady) {
            mediaPlayer.setOnReady(play);
        } else {
            play.run();
        }
    }

    private void play(MediaPlayer mediaPlayer, SongPlayer songPlayer, boolean mediaPlayerNotReady) {
        mediaPlayer.setVolume(0.05);

        // Magic code, without it MediaPlayer makes a weird noise at the beginning of some songs
        if (mediaPlayerNotReady) {
            mediaPlayer.seek(Duration.ZERO);
        }

        mediaPlayer.setOnEndOfMedia(this::skipToNextSong);

        songPlayer.play();

        isSongPlaying = true;
    }

    public void pause() {
        SongPlayer songPlayer = songPlayerProperty.get();
        songPlayer.pause();

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

    public void next() {
        switchSong(++currentSongIndex);
    }

    private void previous() {
        switchSong(currentSongIndex--);
    }

    private void switchSong(int songIndex) {
        dispose();
        SongInfo song = songByIndex(songIndex);
        updateSongInfo(song);
    }

    private void dispose() {
        if (songPlayerProperty.get() == null) {
            return;
        }

        SongPlayer songPlayer = songPlayerProperty.get();
        songPlayer.dispose();
        songPlayerProperty.set(null);
    }

    private void shutdown() {
        System.exit(0);
    }
}
