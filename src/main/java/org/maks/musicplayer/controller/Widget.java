package org.maks.musicplayer.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.maks.musicplayer.components.DurationSlider;
import org.maks.musicplayer.components.PauseToggle;
import org.maks.musicplayer.components.RepeatSongToggle;
import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.enumeration.FXMLPath;
import org.maks.musicplayer.enumeration.Icon;
import org.maks.musicplayer.exception.SongDirectoryEmptyException;
import org.maks.musicplayer.model.Song;
import org.maks.musicplayer.model.SongIndex;
import org.maks.musicplayer.model.SongPlayer;
import org.maks.musicplayer.service.DownloadService;
import org.maks.musicplayer.service.WidgetFXMLLoader;
import org.maks.musicplayer.utils.IconUtils;
import org.maks.musicplayer.utils.PlaylistUtils;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Widget implements Initializable {

    @FXML
    private VBox container;

    @FXML
    private SongInfo songInfo;

    @FXML
    private DurationSlider durationSlider;

    @FXML
    private PauseToggle pauseToggle;

    @FXML
    private RepeatSongToggle repeatSongToggle;

    @FXML
    private ImageView addIcon;

    private final SongIndex currentSongIndex = new SongIndex();
    private final BooleanProperty songPlayingProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<SongPlayer> songPlayerProperty = new SimpleObjectProperty<>();

    private Playlist playlist;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        durationSlider.bindSliderValue(songPlayerProperty);
        pauseToggle.bind(songPlayingProperty);
        songInfo.bind(songPlayerProperty);
        playlist = listMusic();
        addKeybindings();
    }

    private void addKeybindings() {
        KeyCombination next = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
        KeyCodeCombination previous = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);

        container.sceneProperty().addListener((_, _, scene) -> {
            if (scene == null) {
                return;
            }

            scene.setOnKeyPressed(keyEvent -> {
                KeyCode keyCode = keyEvent.getCode();

                if (keyCode == KeyCode.SPACE) {
                    playPause();
                } else if (next.match(keyEvent)) {
                    next();
                } else if (previous.match(keyEvent)) {
                    previous();
                }
            });
        });
    }

    public void loadFirstSong() {
        try {
            Song song = currentSong();
            songPlayerProperty.set(song.songPlayer());
        } catch (SongDirectoryEmptyException e) {
            System.err.println("Songs directory is empty");
        }
    }

    @FXML
    public void playPause() {
        if (songPlayingProperty.get()) {
            pause();
        } else {
            play();
        }
    }

    public void play() {
        if (songPlayerProperty.get() == null) {
            try {
                Song song = currentSong();
                songPlayerProperty.set(song.songPlayer());
            } catch (SongDirectoryEmptyException e) {
                return;
            }
        }

        SongPlayer songPlayer = songPlayerProperty.get();
        MediaPlayer mediaPlayer = songPlayer.mediaPlayer();

        boolean mediaNotPlayerReady = mediaPlayer.getCycleDuration() == Duration.UNKNOWN;
        if (mediaNotPlayerReady) {
            mediaPlayer.setOnReady(() -> {
                play(mediaPlayer, songPlayer, mediaNotPlayerReady);
            });
        } else {
            play(mediaPlayer, songPlayer, mediaNotPlayerReady);
        }
    }

    private void play(MediaPlayer mediaPlayer, SongPlayer songPlayer, boolean mediaPlayerNotReady) {
        Duration duration = mediaPlayer.getCycleDuration();
        mediaPlayer.currentTimeProperty().addListener((
                _,
                _,
                currentDuration) -> {
            if (durationSlider.sliderIsDragged()) {
                return;
            }

            double percentage = (currentDuration.toMillis() / duration.toMillis()) * 100;
            durationSlider.setValue(percentage);
        });

        mediaPlayer.setVolume(0.05);

        // Magic code, without it MediaPlayer makes a weird noise at the beginning of some songs
        if (mediaPlayerNotReady) {
            mediaPlayer.seek(Duration.ZERO);
        }

        mediaPlayer.setOnEndOfMedia(this::skipToNextSong);

        songPlayer.play();

        songPlayingProperty.set(true);
    }

    public void play(int songIndexValue) {
        playSongByNewIndex(songIndex -> songIndex.set(songIndexValue));
    }

    public void pause() {
        SongPlayer songPlayer = songPlayerProperty.get();
        songPlayer.pause();

        songPlayingProperty.set(false);
    }

    private void skipToNextSong() {
        dispose();
        repeatSongToggle.nextSong(this);
    }

    @FXML
    private void addSong() {
        Image initialImage = addIcon.getImage();
        ImageView loadingIcon = IconUtils.icon(Icon.LOADING);
        addIcon.setImage(loadingIcon.getImage());

        DownloadService downloadService = new DownloadService();
        Task<Void> downloadSongTask = downloadService.downloadSongByUrl();

        downloadSongTask.setOnSucceeded(_ -> Platform.runLater(() -> {
            addIcon.setImage(initialImage);
            playlist.refresh(this);
        }));
    }

    @FXML
    public void next() {
        playSongByNewIndex(SongIndex::next);
    }

    @FXML
    public void previous() {
        playSongByNewIndex(SongIndex::previous);
    }

    private void playSongByNewIndex(Consumer<SongIndex> consumer) {
        dispose();
        consumer.accept(currentSongIndex);
        play();
    }

    private void dispose() {
        if (songPlayerProperty.get() == null) {
            return;
        }

        SongPlayer songPlayer = songPlayerProperty.get();
        songPlayer.dispose();
        songPlayerProperty.set(null);
    }

    private Playlist listMusic() {
        WidgetFXMLLoader widgetFXMLLoader = new WidgetFXMLLoader(FXMLPath.MUSIC_LIST);

        Parent musicList = widgetFXMLLoader.parent();
        Playlist playlistController = widgetFXMLLoader.fxmlLoader().getController();
        playlistController.load(this);

        container.getChildren().add(musicList);

        return playlistController;
    }

    @FXML
    private void toggleOnRepeat() {
        repeatSongToggle.toggleOnRepeat();
    }

    @FXML
    private void shutdown() {
        System.exit(0);
    }

    private Song currentSong() throws SongDirectoryEmptyException {
        PlaylistUtils playlistUtils = new PlaylistUtils();
        int amountOfMusic = playlistUtils.amountOfSongs();

        if (amountOfMusic == 0) {
            throw new SongDirectoryEmptyException("Songs directory is empty");
        }

        int index = currentSongIndex.get() % amountOfMusic;

        if (index < 0) {
            index = amountOfMusic + index;
        }

        return playlistUtils.songByIndex(index);
    }

    public ObjectProperty<SongPlayer> songPlayerProperty() {
        return songPlayerProperty;
    }

    public BooleanProperty songPlayingProperty() {
        return songPlayingProperty;
    }

}