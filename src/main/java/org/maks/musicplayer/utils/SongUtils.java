package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.util.Pair;
import org.maks.musicplayer.model.SongDto;
import org.maks.musicplayer.model.SongInfoDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class SongUtils {

    public SongInfoDto songInfoDto(Path songFolderPath) {
        Path songThumbnailPath = findByPredicate(songFolderPath, this::image);
        Path songMediaPath = findByPredicate(songFolderPath, this::media);

        Pair<String, String> songNameAndSongAuthor = songNameAndSongAuthor(songMediaPath);
        String songName = songNameAndSongAuthor.getKey();
        String songAuthor = songNameAndSongAuthor.getValue();

        Image songThumbnail = new Image(songThumbnailPath.toUri().toString());

        return new SongInfoDto(
                songName,
                songAuthor,
                songThumbnail
        );
    }

    public SongDto songDto(Path songFolder) {
        SongInfoDto songInfoDto = songInfoDto(songFolder);

        Path songMediaFile = findByPredicate(songFolder, this::media);
        Media songMedia = new Media(songMediaFile.toUri().toString());

        return new SongDto(
                songInfoDto,
                songMedia
        );
    }

    private Path findByPredicate(Path folderPath, Predicate<Path> predicate) {
        try (var stream = Files.walk(folderPath)) {
            return stream.filter(predicate)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Necessary files not found in: " + folderPath.getFileName()));
        } catch (IOException e) {
            throw new RuntimeException("Song folder is empty");
        }
    }

    private Pair<String, String> songNameAndSongAuthor(Path path) {
        String songAuthor;
        String songName;
        String separator = "\\^";

        String fileNameWithoutExtension = path.getFileName().toString().split("\\.")[0];
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

    private boolean image(Path path) {
        String pathStr = path.toString();
        return pathStr.endsWith(".png") || pathStr.endsWith(".jpg");
    }

    private boolean media(Path path) {
        return path.toString().endsWith(".wav");
    }
}
