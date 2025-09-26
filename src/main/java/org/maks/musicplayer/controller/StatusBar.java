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
import org.maks.musicplayer.components.RoundedImageView;
import org.maks.musicplayer.enumeration.Icon;
import org.maks.musicplayer.exception.SongDirectoryEmptyException;
import org.maks.musicplayer.model.Song;
import org.maks.musicplayer.model.SongIndex;
import org.maks.musicplayer.model.SongInfoDto;
import org.maks.musicplayer.model.SongPlayer;
import org.maks.musicplayer.service.DownloadService;
import org.maks.musicplayer.utils.IconUtils;
import org.maks.musicplayer.utils.ImageUtils;
import org.maks.musicplayer.utils.PlaylistUtils;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class StatusBar implements Initializable {

    @FXML
    private VBox body;

    @FXML
    private RoundedImageView statusBarIcon;

    @FXML
    private Label songName;

    @FXML
    private RepeatSongToggle repeatSongToggle;

    @FXML
    private ImageView addIcon;

    private final SongIndex currentSongIndex = new SongIndex();
    private boolean isSongPlaying = false;
    private final ObjectProperty<SongPlayer> songPlayerProperty = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bindMusicInfo();
        addKeybindings();
        loadFirstSong();
    }

    private void bindMusicInfo() {
        songPlayerProperty.addListener((
                observableValue,
                oldSongPlayer,
                songPlayer) -> {
            if (songPlayer == null) {
                return;
            }

            SongInfoDto songInfoDto = songPlayer.songInfoDto();
            Image songThumbnail = songInfoDto.songThumbnail();
            Image croppedSongThumbnail = ImageUtils.cropToSquare(songThumbnail, statusBarIcon);
            statusBarIcon.setImage(croppedSongThumbnail);
            songName.setText(songInfoDto.songName());
        });
    }

    private void addKeybindings() {
        KeyCombination exit = new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN);
        KeyCombination newSong = new KeyCodeCombination(KeyCode.PLUS, KeyCombination.META_DOWN);

        body.sceneProperty().addListener((v, o, scene) ->
            scene.setOnKeyPressed(keyEvent -> {
                KeyCode keyCode = keyEvent.getCode();

                if (exit.match(keyEvent)) {
                    shutdown();
                }

                if (newSong.match(keyEvent)) {
                    addSong();
                }

                switch (keyCode) {
                    case SPACE -> playPause();
                    case RIGHT -> next();
                    case LEFT -> previous();
                    case R -> repeatSongToggle.toggleOnRepeat();
                }
            })
        );
    }

    private void loadFirstSong() {
        Song song = currentSong();
        songPlayerProperty.set(song.songPlayer());
    }

    private void addSong() {
        Image initialImage = addIcon.getImage();
        ImageView loadingIcon = IconUtils.icon(Icon.LOADING);
        addIcon.setImage(loadingIcon.getImage());

        DownloadService downloadService = new DownloadService();
        downloadService.downloadSong().setOnSucceeded(event ->
                Platform.runLater(() ->
                        addIcon.setImage(initialImage)
                )
        );
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

    private void playPause() {
        if (isSongPlaying) {
            pause();
        } else {
            play();
        }
    }

    public void play() {
        if (songPlayerProperty.get() == null) {
            Song song = currentSong();
            songPlayerProperty.set(song.songPlayer());
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

    public void play(int songIndexValue) {
        playSongByNewIndex(songIndex -> songIndex.set(songIndexValue));
    }

    public void pause() {
        SongPlayer songPlayer = songPlayerProperty.get();
        songPlayer.pause();

        isSongPlaying = false;
    }

    private void skipToNextSong() {
        dispose();
        repeatSongToggle.nextSong(this);
    }

    public void next() {
        playSongByNewIndex(SongIndex::next);
    }

    private void previous() {
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

    private void shutdown() {
        System.exit(0);
    }
}
