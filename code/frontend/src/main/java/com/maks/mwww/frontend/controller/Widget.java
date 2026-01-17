package com.maks.mwww.frontend.controller;

import com.maks.mwww.cqrs.BackendToUIBridge;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.maks.mwww.frontend.components.AddIcon;
import com.maks.mwww.frontend.components.SearchField;
import com.maks.mwww.frontend.components.RepeatToggle;
import com.maks.mwww.frontend.components.ShuffleToggle;
import com.maks.mwww.domain.enumeration.FifoCommand;
import com.maks.mwww.fifo.FifoCommandQueue;
import com.maks.mwww.fifo.FifoCommandSubscriber;
import com.maks.mwww.domain.model.LoadingCallback;
import com.maks.mwww.domain.model.Track;
import com.maks.mwww.backend.player.PlayerService;
import com.maks.mwww.backend.player.PlayerServiceManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
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
    private final PlayerServiceManager playerServiceManager = new PlayerServiceManager();
    private PlayerService<?> playerService = playerServiceManager.getPlayerService(bridge);

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
                PlayerService<?> newPlayerService = playerServiceManager.getPlayerService(context, bridge);

                if (newPlayerService == null) {
                    LOG.warning("Ignoring invalid context: " + context);
                } else {
                    updatePlayerService(newPlayerService);
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
                    addTrack();
                } else if (deleteTrack.match(keyEvent)) {
                    deleteTrack();
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
        thumbnail.setImage(new Image(track.thumbnailUrl()));
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

    private void addTrack() {
        addIcon.loading();

        CompletableFuture<Void> task = playerService.addTrack();

        task.whenComplete((_, ex) -> {
            if (ex != null) {
                addIcon.fail();
                LOG.severe(ex.getMessage());
            } else {
                addIcon.like();
            }
        });
    }

    private void deleteTrack() {
        addIcon.loading();

        CompletableFuture<Void> task = playerService.deleteTrack();

        task.whenComplete((_, ex) -> {
            if (ex != null) {
                addIcon.fail();
                LOG.severe(ex.getMessage());
            } else {
                addIcon.success();
            }
        });
    }

    private void stageOp(Consumer<Stage> op) {
        Stage stage = (Stage) body.getScene().getWindow();
        op.accept(stage);
    }

    private void shutdown() {
        System.exit(0);
    }
}
