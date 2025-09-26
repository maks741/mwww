package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.util.Pair;
import org.maks.musicplayer.model.SongInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SongUtils {

    public SongInfo songInfo(Path songFolderPath) {
        Path songThumbnailPath = findByPredicate(songFolderPath, this::isImage);

        Pair<String, String> songNameAndSongAuthor = songNameAndSongAuthor(songFolderPath);
        String songName = songNameAndSongAuthor.getKey();
        String songAuthor = songNameAndSongAuthor.getValue();

        Image songThumbnail = new Image(songThumbnailPath.toUri().toString());

        return new SongInfo(
                songName,
                songAuthor,
                songThumbnail
        );
    }

    public Media media(Path songFolder) {
        Path songMediaFile = findByPredicate(songFolder, this::isMedia);
        return new Media(songMediaFile.toUri().toString());
    }

    private Path findByPredicate(Path folderPath, Predicate<Path> predicate) {
        try (Stream<Path> stream = Files.list(folderPath)) {
            return stream.filter(predicate)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Necessary files not found in: " + folderPath.getFileName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<String, String> songNameAndSongAuthor(Path songDirPath) {
        String songAuthor;
        String songName;
        String separator = "\\^";

        String fileNameWithoutExtension = songDirPath.getFileName().toString();
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

    private boolean isImage(Path path) {
        String pathStr = path.toString();
        return pathStr.endsWith(".png") || pathStr.endsWith(".jpg");
    }

    private boolean isMedia(Path path) {
        return path.toString().endsWith(".wav");
    }
}
