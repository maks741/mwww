package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Pair;
import org.maks.musicplayer.model.SongInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class PlaylistUtils {

    public SongInfo songInfo(int index) {
        try (Stream<Path> songDirs = Files.list(Paths.get("songs"))) {
            Path songFolderPath = songDirs
                    .skip(index)
                    .findFirst()
                    .orElseThrow();

            Path songThumbnailPath = songFolderPath.resolve("img.png");

            Pair<String, String> songNameAndSongAuthor = songNameAndSongAuthor(songFolderPath);
            String songName = songNameAndSongAuthor.getKey();
            String songAuthor = songNameAndSongAuthor.getValue();

            Image songThumbnail = new Image(songThumbnailPath.toUri().toString());

            return new SongInfo(
                    songName,
                    songAuthor,
                    songThumbnail
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MediaPlayer player(int index) {
        try (Stream<Path> songDirs = Files.list(Paths.get("songs"))) {
            Path songDirPath = songDirs
                    .skip(index)
                    .findFirst()
                    .orElseThrow();

            Path songMediaFile = songDirPath.resolve("media.wav");
            Media media = new Media(songMediaFile.toUri().toString());
            return new MediaPlayer(media);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int amountOfSongs() {
        try (Stream<Path> songDirs = Files.list(Paths.get("songs"))) {
            return (int) songDirs.count();
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
}
