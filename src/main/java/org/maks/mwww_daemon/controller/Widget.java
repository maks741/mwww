package org.maks.mwww_daemon.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.maks.mwww_daemon.components.AddIcon;
import org.maks.mwww_daemon.components.DynamicLabel;
import org.maks.mwww_daemon.components.RepeatToggle;
import org.maks.mwww_daemon.components.ShuffleToggle;
import org.maks.mwww_daemon.enumeration.FifoCommand;
import org.maks.mwww_daemon.fifo.FifoCommandQueue;
import org.maks.mwww_daemon.fifo.FifoCommandSubscriber;
import org.maks.mwww_daemon.model.BaseSongInfo;
import org.maks.mwww_daemon.service.PlayerService;
import org.maks.mwww_daemon.service.local.LocalPlayerService;
import org.maks.mwww_daemon.service.spotify.SpotifyPlayerService;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Widget implements Initializable, FifoCommandSubscriber {

    private static final Logger LOG = Logger.getLogger(Widget.class.getName());

    @FXML
    private VBox body;

    @FXML
    private ImageView statusBarIcon;

    @FXML
    private DynamicLabel dynamicSongName;

    @FXML
    private RepeatToggle repeatToggle;

    @FXML
    private ShuffleToggle shuffleToggle;

    @FXML
    private AddIcon addIcon;

    private PlayerService<?> playerService = new SpotifyPlayerService(this::onSongUpdated);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playerService.initialize();
        dynamicSongName.setOnSubmit(this.playerService::switchSong);
        addKeybindings();

        Runtime.getRuntime().addShutdownHook(new Thread(playerService::shutdown));
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
            case SET_CONTEXT -> {
                String context = command.getValue();

                if (context.equals("local")) {
                    updatePlayerService(new LocalPlayerService(this::onSongUpdated));
                } else if (context.equals("spotify")) {
                    updatePlayerService(new SpotifyPlayerService(this::onSongUpdated));
                } else {
                    LOG.warning("Ignoring invalid context: " + context);
                }
            }
        }
    }

    private void addKeybindings() {
        KeyCombination next = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
        KeyCombination previous = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);
        KeyCombination volumeUp = new KeyCodeCombination(KeyCode.F3);
        KeyCombination volumeDown = new KeyCodeCombination(KeyCode.F2);
        KeyCombination skipForward = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHIFT_DOWN);
        KeyCombination skipBackward = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_DOWN);
        KeyCombination toggleRepeat = new KeyCodeCombination(KeyCode.R);
        KeyCombination toggleShuffle = new KeyCodeCombination(KeyCode.S);
        KeyCombination togglePause = new KeyCodeCombination(KeyCode.P);
        KeyCombination find = new KeyCodeCombination(KeyCode.F);
        KeyCombination newSong = new KeyCodeCombination(KeyCode.N);
        KeyCombination deleteSong = new KeyCodeCombination(KeyCode.D);
        KeyCombination exit = new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN, KeyCombination.CONTROL_DOWN);

        body.sceneProperty().addListener((_, _, scene) -> {
            scene.setOnKeyPressed(keyEvent -> {
                if (next.match(keyEvent)) {
                    playerService.next();
                } else if (previous.match(keyEvent)) {
                    playerService.previous();
                } else if (volumeUp.match(keyEvent)) {
                    playerService.volumeUp();
                } else if (volumeDown.match(keyEvent)) {
                    playerService.volumeDown();
                } else if (skipForward.match(keyEvent)) {
                    playerService.skipForward();
                } else if (skipBackward.match(keyEvent)) {
                    playerService.skipBackward();
                } else if (toggleRepeat.match(keyEvent)) {
                    if (playerService.toggleRepeat()) {
                        repeatToggle.toggle();
                    }
                } else if (toggleShuffle.match(keyEvent)) {
                    if (playerService.toggleShuffle()) {
                        shuffleToggle.toggle();
                    }
                } else if (togglePause.match(keyEvent)) {
                    playerService.togglePause();
                } else if (find.match(keyEvent)) {
                    dynamicSongName.switchToTextField();
                } else if (newSong.match(keyEvent)) {
                    playerService.addSong(addIcon, dynamicSongName);
                } else if (deleteSong.match(keyEvent)) {
                    playerService.deleteSong(addIcon);
                } else if (exit.match(keyEvent)) {
                    shutdown();
                }
            });

            scene.setOnKeyReleased(keyEvent -> {
                if (KeyCode.CONTROL == keyEvent.getCode()) {
                    playerService.play();
                }
            });
        });
    }

    private void onSongUpdated(BaseSongInfo songInfo) {
        statusBarIcon.setImage(songInfo.thumbnail());
        dynamicSongName.setText(songInfo.title());
    }

    private void updatePlayerService(PlayerService<?> playerService) {
        shuffleToggle.reset();
        repeatToggle.reset();

        this.playerService.shutdown();
        this.playerService = playerService;
        this.playerService.initialize();
    }

    private void stageOp(Consumer<Stage> op) {
        Stage stage = (Stage) body.getScene().getWindow();
        op.accept(stage);
    }

    private void shutdown() {
        System.exit(0);
    }
}
