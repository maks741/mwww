package org.maks.mwww_daemon.controller;

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
import javafx.stage.Stage;
import javafx.util.Duration;
import org.maks.mwww_daemon.components.RepeatSongToggle;
import org.maks.mwww_daemon.enumeration.FifoCommand;
import org.maks.mwww_daemon.enumeration.Icon;
import org.maks.mwww_daemon.fifo.FifoCommandQueue;
import org.maks.mwww_daemon.fifo.FifoCommandSubscriber;
import org.maks.mwww_daemon.model.SongInfo;
import org.maks.mwww_daemon.service.DownloadService;
import org.maks.mwww_daemon.utils.IconUtils;
import org.maks.mwww_daemon.utils.PlaylistUtils;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Widget implements Initializable, FifoCommandSubscriber {

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

    private Duration skipDuration = Duration.seconds(10);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadFirstSong();
        addKeybindings();
    }

    @Override
    public void accept(FifoCommandQueue observable, FifoCommand command) {
        switch (command) {
            case HIDE -> {
                Platform.setImplicitExit(false);
                stageOp(Stage::hide);
            }
            case SHOW -> {
                stageOp(Stage::show);
                Platform.setImplicitExit(true);
            }
            case SET_SONG -> switchSong(command.getValue());
            case SET_SKIP_DURATION -> skipDuration = Duration.seconds(command.getValueAsInt());
        }
    }

    private void loadFirstSong() {
        SongInfo song = lookupSong(currentSongIndex);
        updateSongInfo(song);
    }

    private void updateSongInfo(SongInfo songInfo) {
        statusBarIcon.setImage(songInfo.songThumbnail());
        songName.setText(songInfo.songName());
        currentSongIndex = songInfo.songIndex();
    }

    private void addKeybindings() {
        KeyCombination next = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
        KeyCombination previous = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
        KeyCombination skipForward = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHIFT_DOWN);
        KeyCombination skipBackward = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_DOWN);
        KeyCombination toggleRepeat = new KeyCodeCombination(KeyCode.R);
        KeyCombination togglePause = new KeyCodeCombination(KeyCode.P);
        KeyCombination newSong = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        KeyCombination deleteSong = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
        KeyCombination exit = new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN, KeyCombination.CONTROL_DOWN);

        body.sceneProperty().addListener((_, _, scene) -> {
            scene.setOnKeyPressed(keyEvent -> {
                if (next.match(keyEvent)) {
                    next();
                } else if (previous.match(keyEvent)) {
                    previous();
                } else if (skipForward.match(keyEvent)) {
                    skipForward();
                } else if (skipBackward.match(keyEvent)) {
                    skipBackward();
                } else if (toggleRepeat.match(keyEvent)) {
                    repeatSongToggle.toggleOnRepeat();
                } else if (togglePause.match(keyEvent)) {
                    togglePause();
                } else if (newSong.match(keyEvent)) {
                    addSong();
                } else if (deleteSong.match(keyEvent)) {
                    deleteSong();
                } else if (exit.match(keyEvent)) {
                    shutdown();
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
        CompletableFuture<String> task = downloadService.downloadSong();
        task.whenComplete((downloadedSongName, ex) -> {
            if (ex == null) {
                Platform.runLater(() -> switchSong(downloadedSongName));
            }

            addIcon.setImage(initialImage);
        });
    }

    private void deleteSong() {
        PlaylistUtils playlistUtils = new PlaylistUtils();
        playlistUtils.deleteSong(currentSongIndex);
        reloadCurrent();
    }

    private SongInfo lookupSong(int songIndex) {
        PlaylistUtils playlistUtils = new PlaylistUtils();
        return playlistUtils.songInfo(songIndex);
    }

    private SongInfo lookupSong(String songName) {
        PlaylistUtils playlistUtils = new PlaylistUtils();
        return playlistUtils.songInfo(songName);
    }

    private void togglePause() {
        if (currentPlayer == null) {
            return;
        }

        if (isSongPlaying) {
            currentPlayer.pause();
        } else {
            currentPlayer.play();
        }

        isSongPlaying = !isSongPlaying;
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

    private void skipToNextSong() {
        dispose();

        if (!repeatSongToggle.onRepeat()) {
            next();
        }

        play();
    }

    private void reloadCurrent() {
        switchSong(currentSongIndex);
    }

    private void next() {
        switchSong(++currentSongIndex);
    }

    private void previous() {
        switchSong(--currentSongIndex);
    }

    private void skipForward() {
        skip(Duration::add);
    }

    private void skipBackward() {
        skip(Duration::subtract);
    }

    private void skip(BiFunction<Duration, Duration, Duration> operation) {
        if (currentPlayer == null) {
            return;
        }

        Duration currentDuration = currentPlayer.getCurrentTime();
        Duration newDuration = operation.apply(currentDuration, skipDuration);
        currentPlayer.seek(newDuration);
    }

    private void switchSong(int songIndex) {
        switchSong(lookupSong(songIndex));
    }

    private void switchSong(String songName) {
        switchSong(lookupSong(songName));
    }

    private void switchSong(SongInfo songInfo) {
        dispose();
        updateSongInfo(songInfo);
    }

    private void dispose() {
        if (currentPlayer == null) {
            return;
        }

        currentPlayer.dispose();
        currentPlayer = null;
    }

    private void stageOp(Consumer<Stage> op) {
        Stage stage = (Stage) body.getScene().getWindow();
        op.accept(stage);
    }

    private void shutdown() {
        System.exit(0);
    }
}
