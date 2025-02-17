package org.maks.musicplayer.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.maks.musicplayer.components.DurationSlider;
import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.components.PauseToggle;
import org.maks.musicplayer.components.RepeatSongToggle;
import org.maks.musicplayer.enumeration.FXMLPath;
import org.maks.musicplayer.enumeration.IconName;
import org.maks.musicplayer.model.SongPlayer;
import org.maks.musicplayer.model.Song;
import org.maks.musicplayer.model.SongIndex;
import org.maks.musicplayer.service.DownloadService;
import org.maks.musicplayer.utils.IconUtils;
import org.maks.musicplayer.utils.PlaylistUtils;
import org.maks.musicplayer.service.WidgetFXMLLoader;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
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

    private final AtomicReference<SongPlayer> currentMediaContainer = new AtomicReference<>();
    private final SongIndex currentSongIndex = new SongIndex();
    private final BooleanProperty songPlayingProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<SongPlayer> mediaPlayerContainerProperty = new SimpleObjectProperty<>();

    private Playlist playlist;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        durationSlider.bindSliderValue(currentMediaContainer);
        pauseToggle.bind(songPlayingProperty);
        songInfo.bind(mediaPlayerContainerProperty);
        playlist = listMusic();
    }

    public void loadFirstSong() {
        MediaPlayer mediaPlayer = play();
        mediaPlayer.setOnPlaying(() -> {
            mediaPlayer.setOnPlaying(null);
            pause();
            mediaPlayer.seek(Duration.ZERO);
        });
    }

    @FXML
    public void playPause() {
        if (songPlayingProperty.get()) {
            pause();
        } else {
            play();
        }
    }

    public MediaPlayer play() {
        if (currentMediaContainer.get() == null) {
            currentMediaContainer.set(currentMusic().songPlayer());
        }

        SongPlayer songPlayer = currentMediaContainer.get();
        mediaPlayerContainerProperty.set(songPlayer);

        MediaPlayer mediaPlayer = songPlayer.mediaPlayer();

        boolean mediaNotPlayerReady = mediaPlayer.getCycleDuration() == Duration.UNKNOWN;
        if (mediaNotPlayerReady) {
            mediaPlayer.setOnReady(() -> play(mediaPlayer, songPlayer));
        } else {
            play(mediaPlayer, songPlayer);
        }

        return mediaPlayer;
    }

    private void play(MediaPlayer mediaPlayer, SongPlayer songPlayer) {
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
        mediaPlayer.setOnEndOfMedia(this::skipToNextSong);
        songPlayer.play();

        pauseToggle.onMusicPlayed();
        songPlayingProperty.set(true);
    }

    public void play(int songIndexValue) {
        playSongByNewIndex(songIndex -> songIndex.set(songIndexValue));
    }

    public void pause() {
        SongPlayer songPlayer = currentMediaContainer.get();
        songPlayer.pause();

        pauseToggle.onMusicPaused();
        songPlayingProperty.set(false);
    }

    private void skipToNextSong() {
        dispose();
        repeatSongToggle.nextSong(this);
    }

    @FXML
    private void addSong() {
        Image initialImage = addIcon.getImage();
        ImageView loadingIcon = IconUtils.icon(IconName.LOADING);
        addIcon.setImage(loadingIcon.getImage());

        DownloadService downloadService = new DownloadService();
        CompletableFuture<String> downloadedSongDirectoryFuture = downloadService.downloadSongByUrl();

        downloadedSongDirectoryFuture.thenAccept(downloadedSongDirectory -> {
            Platform.runLater(() -> addIcon.setImage(initialImage));

            if (downloadedSongDirectory.isEmpty()) {
                return;
            }

            Platform.runLater(() -> playlist.add(
                    this,
                    downloadedSongDirectory
            ));
        });
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
        if (currentMediaContainer.get() == null) return;

        SongPlayer songPlayer = currentMediaContainer.get();
        songPlayer.refresh();
        currentMediaContainer.set(null);
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

    private Song currentMusic() {
        List<Song> songList = PlaylistUtils.playlist();
        int amountOfMusic = songList.size();

        int index = currentSongIndex.get() % amountOfMusic;

        if (index < 0) {
            index = amountOfMusic + index;
        }

        return songList.get(index);
    }

    public ObjectProperty<SongPlayer> mediaPlayerContainerProperty() {
        return mediaPlayerContainerProperty;
    }

    public BooleanProperty songPlayingProperty() {
        return songPlayingProperty;
    }

}