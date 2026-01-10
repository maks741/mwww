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
import org.maks.mwww_daemon.components.SearchField;
import org.maks.mwww_daemon.components.RepeatToggle;
import org.maks.mwww_daemon.components.ShuffleToggle;
import org.maks.mwww_daemon.enumeration.FifoCommand;
import org.maks.mwww_daemon.enumeration.PlayerContext;
import org.maks.mwww_daemon.fifo.FifoCommandQueue;
import org.maks.mwww_daemon.fifo.FifoCommandSubscriber;
import org.maks.mwww_daemon.model.Track;
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
    private ImageView thumbnail;

    @FXML
    private SearchField searchField;

    @FXML
    private RepeatToggle repeatToggle;

    @FXML
    private ShuffleToggle shuffleToggle;

    @FXML
    private AddIcon addIcon;

    private PlayerService<?> playerService = new SpotifyPlayerService(this::onTrackUpdated);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playerService.initialize();
        searchField.setOnSubmit(this.playerService::switchTrack);
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

                if (context.equals(PlayerContext.LOCAL.context())) {
                    updatePlayerService(new LocalPlayerService(this::onTrackUpdated));
                } else if (context.equals(PlayerContext.SPOTIFY.context())) {
                    updatePlayerService(new SpotifyPlayerService(this::onTrackUpdated));
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
        KeyCombination addTrack = new KeyCodeCombination(KeyCode.N);
        KeyCombination deleteTrack = new KeyCodeCombination(KeyCode.D);
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
                    searchField.switchToTextField();
                } else if (addTrack.match(keyEvent)) {
                    playerService.addTrack(addIcon, searchField);
                } else if (deleteTrack.match(keyEvent)) {
                    playerService.deleteTrack(addIcon);
                } else if (exit.match(keyEvent)) {
                    shutdown();
                }
            });

            scene.setOnKeyReleased(keyEvent -> {
                if (KeyCode.CONTROL == keyEvent.getCode()) {
                    // ignore Ctrl-related events when search field is active (e.g. Ctrl + V)
                    if (searchField.isSearching()) {
                        return;
                    }

                    playerService.play();
                }
            });
        });
    }

    private void onTrackUpdated(Track track) {
        thumbnail.setImage(track.thumbnail());
        searchField.setText(track.title());
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
