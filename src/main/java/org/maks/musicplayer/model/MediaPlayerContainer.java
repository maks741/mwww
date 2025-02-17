package org.maks.musicplayer.model;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.function.Predicate;

public class MediaPlayerContainer {

    private final File songFolder;
    private MediaPlayer mediaPlayer;
    private final String songName;
    private final String songAuthor;
    private final Image songAvatar;

    public MediaPlayerContainer(File songFolder) {
        this.songFolder = songFolder;
        File songFile = findFirst(songFolder, this::wavFile);

        Pair<String, String> songNameAndSongAuthor = songNameAndSongAuthor(songFile);
        this.songName = songNameAndSongAuthor.getKey();
        this.songAuthor = songNameAndSongAuthor.getValue();

        File songAvatarFile = findFirst(songFolder, this::image);
        this.songAvatar = new Image(songAvatarFile.toURI().toString());

        Media media = new Media(songFile.toURI().toString());
        this.mediaPlayer = new MediaPlayer(media);
    }

    public void play() {
        mediaPlayer.play();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void dispose() {
        mediaPlayer.stop();
        mediaPlayer.dispose();

        File songFile = findFirst(songFolder, this::wavFile);
        Media media = new Media(songFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
    }

    private File findFirst(File songFolder, Predicate<File> predicate) {
        File[] files = songFolder.listFiles();

        if (files == null) {
            throw new RuntimeException("Song folder is empty");
        }

        return Arrays.stream(files)
                .filter(predicate)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Necessary files not found"));
    }

    private Pair<String, String> songNameAndSongAuthor(File file) {
        String songAuthor;
        String songName;

        String fileNameWithoutExtension = file.getName().split("\\.")[0];
        String[] parts = fileNameWithoutExtension.split("_");
        if (parts.length >= 2) {
            songAuthor = parts[0];
            songName = parts[1];
        } else if (parts.length == 1) {
            songAuthor = "Unknown";
            songName = parts[0];
        } else {
            songAuthor = "Unknown";
            songName = "Unknown";
        }

        return new Pair<>(songName, songAuthor);
    }

    private boolean image(File file) {
        String fileName = file.getName();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg");
    }

    private boolean wavFile(File file) {
        return file.getName().endsWith(".wav");
    }

    public MediaPlayer mediaPlayer() {
        return mediaPlayer;
    }

    public Image songAvatar() {
        return songAvatar;
    }

    public String songName() {
        return songName;
    }

    public String songAuthor() {
        return songAuthor;
    }

}
