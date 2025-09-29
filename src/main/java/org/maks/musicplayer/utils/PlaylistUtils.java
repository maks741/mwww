package org.maks.musicplayer.utils;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Pair;
import org.maks.musicplayer.exception.SongDirectoryEmptyException;
import org.maks.musicplayer.model.SongInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class PlaylistUtils {

    public SongInfo songInfo(int index) {
        int normalizedIndex = normalizeIndex(index);

        try (Stream<Path> songDirs = Files.list(Paths.get("songs"))) {
            Path songDirPath = songDirs
                    .skip(normalizedIndex)
                    .findFirst()
                    .orElseThrow();

            return songInfo(songDirPath, normalizedIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SongInfo songInfo(String targetSongName) {
        AtomicInteger songIndexCounter = new AtomicInteger(0);

        try (Stream<Path> songDirs = Files.list(Paths.get("songs"))) {
            Path songDirPath = songDirs
                    .filter(path -> {
                        songIndexCounter.getAndIncrement();
                        return path.getFileName().toString().contains(targetSongName);
                    })
                    .findFirst()
                    .orElseThrow();

            return songInfo(songDirPath, songIndexCounter.decrementAndGet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MediaPlayer player(int index) {
        int normalizedIndex = normalizeIndex(index);

        try (Stream<Path> songDirs = Files.list(Paths.get("songs"))) {
            Path songDirPath = songDirs
                    .skip(normalizedIndex)
                    .findFirst()
                    .orElseThrow();

            Path songMediaFile = songDirPath.resolve("media.wav");
            Media media = new Media(songMediaFile.toUri().toString());
            return new MediaPlayer(media);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SongInfo songInfo(Path songDirPath, int songIndex) {
        Path songThumbnailPath = songDirPath.resolve("img.png");

        Pair<String, String> songNameAndSongAuthor = songNameAndSongAuthor(songDirPath);
        String songName = songNameAndSongAuthor.getKey();
        String songAuthor = songNameAndSongAuthor.getValue();

        Image songThumbnail = new Image(songThumbnailPath.toUri().toString());

        return new SongInfo(
                songName,
                songAuthor,
                songThumbnail,
                songIndex
        );
    }

    private int normalizeIndex(int index) {
        try (Stream<Path> songDirs = Files.list(Paths.get("songs"))) {
            int amountOfSongs = (int) songDirs.count();

            if (amountOfSongs == 0) {
                throw new SongDirectoryEmptyException("Songs directory is empty");
            }

            int normalizedIndex = index % amountOfSongs;

            if (normalizedIndex < 0) {
                normalizedIndex = amountOfSongs + normalizedIndex;
            }

            return normalizedIndex;
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
