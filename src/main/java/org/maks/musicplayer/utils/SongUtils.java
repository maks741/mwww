package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.util.Pair;
import org.maks.musicplayer.model.SongDto;
import org.maks.musicplayer.model.SongInfoDto;

import java.io.File;
import java.util.Arrays;
import java.util.function.Predicate;

public class SongUtils {

    public SongInfoDto songInfoDto(File songFolder) {
        File songThumbnailFile = lookupFolderByPredicate(songFolder, this::image);
        File songMediaFile = lookupFolderByPredicate(songFolder, this::media);

        Pair<String, String> songNameAndSongAuthor = songNameAndSongAuthor(songMediaFile);
        String songName = songNameAndSongAuthor.getKey();
        String songAuthor = songNameAndSongAuthor.getValue();

        Image songThumbnail = new Image(songThumbnailFile.toURI().toString());

        return new SongInfoDto(
                songName,
                songAuthor,
                songThumbnail
        );
    }

    public SongDto songDto(File songFolder) {
        SongInfoDto songInfoDto = songInfoDto(songFolder);

        File songMediaFile = lookupFolderByPredicate(songFolder, this::media);
        Media songMedia = new Media(songMediaFile.toURI().toString());

        return new SongDto(
                songInfoDto,
                songMedia
        );
    }

    private File lookupFolderByPredicate(File songFolder, Predicate<File> predicate) {
        File[] files = songFolder.listFiles();

        if (files == null) {
            throw new RuntimeException("Song folder is empty");
        }

        return Arrays.stream(files)
                .filter(predicate)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Necessary files not found in: " + songFolder.getName()));
    }

    private Pair<String, String> songNameAndSongAuthor(File file) {
        String songAuthor;
        String songName;
        String separator = "\\^";

        String fileNameWithoutExtension = file.getName().split("\\.")[0];
        String[] parts = fileNameWithoutExtension.split(separator);
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
