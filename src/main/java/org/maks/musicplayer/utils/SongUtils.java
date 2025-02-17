package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.util.Pair;
import org.maks.musicplayer.model.SongDetails;

import java.io.File;
import java.util.Arrays;
import java.util.function.Predicate;

public class SongUtils {

    public SongDetails songDetails(File songFolder) {
        File songThumbnailFile = findFirst(songFolder, this::image);
        File songMediaFile = findFirst(songFolder, this::media);

        Pair<String, String> songNameAndSongAuthor = songNameAndSongAuthor(songMediaFile);
        String songName = songNameAndSongAuthor.getKey();
        String songAuthor = songNameAndSongAuthor.getValue();

        Media songMedia = new Media(songMediaFile.toURI().toString());
        Image songThumbnail = new Image(songThumbnailFile.toURI().toString());

        return new SongDetails(
                songName,
                songAuthor,
                songThumbnail,
                songMedia
        );
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

    private boolean media(File file) {
        return file.getName().endsWith(".wav");
    }

}
