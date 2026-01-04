package org.maks.mwww_daemon.service.local;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Pair;
import org.maks.mwww_daemon.exception.SongDirectoryEmptyException;
import org.maks.mwww_daemon.model.NotFoundSongInfo;
import org.maks.mwww_daemon.model.LocalSongInfo;
import org.maks.mwww_daemon.utils.ResourceUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class LocalPlaylistUtils {

    public LocalSongInfo songInfo(int index) {
        int normalizedIndex;

        try {
            normalizedIndex = normalizeIndex(index);
        } catch (SongDirectoryEmptyException e) {
            return new LocalSongInfo("Use Ctrl + N to add a new song");
        }

        try (Stream<Path> songDirs = Files.list(ResourceUtils.songsDirPath())) {
            Path songDirPath = songDirs
                    .skip(normalizedIndex)
                    .findFirst()
                    .orElseThrow();

            return songInfo(songDirPath, normalizedIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalSongInfo songInfo(String targetSongName) {
        AtomicInteger songIndexCounter = new AtomicInteger(0);

        try (Stream<Path> songDirs = Files.list(ResourceUtils.songsDirPath())) {
            return songDirs
                    .filter(path -> {
                        songIndexCounter.getAndIncrement();
                        return path.getFileName().toString().contains(targetSongName);
                    })
                    .findFirst()
                    .map(path -> songInfo(path, songIndexCounter.decrementAndGet()))
                    .orElse(new NotFoundSongInfo());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MediaPlayer player(int index) {
        int normalizedIndex = safeNormalizeIndex(index);

        try (Stream<Path> songDirs = Files.list(ResourceUtils.songsDirPath())) {
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

    public void deleteSong(int index) {
        int normalizedIndex = safeNormalizeIndex(index);

        try (Stream<Path> songDirs = Files.list(ResourceUtils.songsDirPath())) {
            Path songDirPath = songDirs
                    .skip(normalizedIndex)
                    .findFirst()
                    .orElseThrow();

            Files.walkFileTree(songDirPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int count() {
        try (Stream<Path> songDirs = Files.list(ResourceUtils.songsDirPath())) {
            return  (int) songDirs.count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalSongInfo songInfo(Path songDirPath, int songIndex) {
        Path songThumbnailPath = songDirPath.resolve("img.png");

        Pair<String, String> songNameAndSongAuthor = songNameAndSongAuthor(songDirPath);
        String songName = songNameAndSongAuthor.getKey();
        String songAuthor = songNameAndSongAuthor.getValue();

        Image songThumbnail = new Image(songThumbnailPath.toUri().toString());

        return new LocalSongInfo(
                songName,
                songAuthor,
                songThumbnail,
                songIndex
        );
    }

    private int normalizeIndex(int index) throws SongDirectoryEmptyException {
        try (Stream<Path> songDirs = Files.list(ResourceUtils.songsDirPath())) {
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

    private int safeNormalizeIndex(int index) {
        try {
            return normalizeIndex(index);
        } catch (SongDirectoryEmptyException e) {
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
