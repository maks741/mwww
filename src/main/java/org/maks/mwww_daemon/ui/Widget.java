package org.maks.mwww_daemon.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.maks.mwww_daemon.ui.components.AddIcon;
import org.maks.mwww_daemon.ui.components.SearchField;
import org.maks.mwww_daemon.ui.components.RepeatToggle;
import org.maks.mwww_daemon.ui.components.ShuffleToggle;
import org.maks.mwww_daemon.shared.domain.enumeration.FifoCommand;
import org.maks.mwww_daemon.shared.domain.enumeration.PlayerContext;
import org.maks.mwww_daemon.backend.fifo.FifoCommandQueue;
import org.maks.mwww_daemon.backend.fifo.FifoCommandSubscriber;
import org.maks.mwww_daemon.shared.domain.model.LoadingCallback;
import org.maks.mwww_daemon.shared.domain.model.Track;
import org.maks.mwww_daemon.backend.service.BackendToUIBridge;
import org.maks.mwww_daemon.backend.service.PlayerService;
import org.maks.mwww_daemon.backend.local.LocalPlayerService;
import org.maks.mwww_daemon.backend.spotify.SpotifyPlayerService;

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

    private final BackendToUIBridge bridge = new BackendToUIBridge();
    private PlayerService<?> playerService = new SpotifyPlayerService(bridge);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bridge.setOnTrackUpdated(this::onTrackUpdated);
        bridge.setOnRequestLoading(this::onRequestLoading);

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
                    updatePlayerService(new LocalPlayerService(bridge));
                } else if (context.equals(PlayerContext.SPOTIFY.context())) {
                    updatePlayerService(new SpotifyPlayerService(bridge));
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

    private void onRequestLoading(LoadingCallback loadingCallback) {
        addIcon.loading();

        String initialText = searchField.text();
        searchField.setText(loadingCallback.message());

        loadingCallback.setOnCallback((ex) -> {
            if (ex == null) {
                addIcon.reset();
            } else {
                addIcon.fail();
            }

            searchField.setText(initialText);
        });
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
