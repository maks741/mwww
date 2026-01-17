package com.maks.mwww.backend.local;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Pair;
import com.maks.mwww.domain.exception.EmptyTracksDirException;
import com.maks.mwww.domain.model.LocalTrack;
import com.maks.mwww.backend.utils.ResourceUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class LocalPlaylistUtils {

    public LocalTrack firstTrack() {
        int index;

        try {
            index = firstTrackIndex();
        } catch (EmptyTracksDirException e) {
            return new LocalTrack("Use Ctrl + N to add a new track");
        }

        return trackInfoByIndex(index);
    }

    public LocalTrack trackInfo(int index) {
        int normalizedIndex;

        try {
            normalizedIndex = normalizeIndex(index);
        } catch (EmptyTracksDirException e) {
            return new LocalTrack("Use Ctrl + N to add a new trac");
        }

        return trackInfoByIndex(normalizedIndex);
    }

    public LocalTrack trackInfo(String targetTrackName) {
        AtomicInteger trackIndexCounter = new AtomicInteger(0);

        try (Stream<Path> trackDirs = Files.list(ResourceUtils.tracksDirPath())) {
            return trackDirs
                    .filter(path -> {
                        trackIndexCounter.getAndIncrement();
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.contains(targetTrackName.toLowerCase());
                    })
                    .findFirst()
                    .map(path -> trackInfo(path, trackIndexCounter.decrementAndGet()))
                    .orElse(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MediaPlayer player(int index) {
        int normalizedIndex = safeNormalizeIndex(index);

        try (Stream<Path> trackDirs = Files.list(ResourceUtils.tracksDirPath())) {
            Path trackDirPath = trackDirs
                    .skip(normalizedIndex)
                    .findFirst()
                    .orElseThrow();

            Path trackMediaFile = trackDirPath.resolve("media.wav");
            Media media = new Media(trackMediaFile.toUri().toString());
            return new MediaPlayer(media);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTrack(int index) {
        int normalizedIndex = safeNormalizeIndex(index);

        try (Stream<Path> trackDirs = Files.list(ResourceUtils.tracksDirPath())) {
            Path trackDirPath = trackDirs
                    .skip(normalizedIndex)
                    .findFirst()
                    .orElseThrow();

            Files.walkFileTree(trackDirPath, new SimpleFileVisitor<>() {
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
        try (Stream<Path> trackDirs = Files.list(ResourceUtils.tracksDirPath())) {
            return  (int) trackDirs.count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalTrack trackInfo(Path trackDirPath, int trackIndex) {
        Path trackThumbnailPath = trackDirPath.resolve("img.png");

        Pair<String, String> titleAndArtist = titleAndArtist(trackDirPath);
        String title = titleAndArtist.getKey();
        String artist = titleAndArtist.getValue();

        String thumbnailUri = trackThumbnailPath.toUri().toString();

        return new LocalTrack(
                title,
                artist,
                thumbnailUri,
                trackIndex
        );
    }

    private LocalTrack trackInfoByIndex(int index) {
        try (Stream<Path> trackDirs = Files.list(ResourceUtils.tracksDirPath())) {
            Path trackDirPath = trackDirs
                    .skip(index)
                    .findFirst()
                    .orElseThrow();

            return trackInfo(trackDirPath, index);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int normalizeIndex(int index) throws EmptyTracksDirException {
        try (Stream<Path> trackDirs = Files.list(ResourceUtils.tracksDirPath())) {
            int amountOfTracks = (int) trackDirs.count();

            if (amountOfTracks == 0) {
                throw new EmptyTracksDirException("Tracks directory is empty");
            }

            int normalizedIndex = index % amountOfTracks;

            if (normalizedIndex < 0) {
                normalizedIndex = amountOfTracks + normalizedIndex;
            }

            return normalizedIndex;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int safeNormalizeIndex(int index) {
        try {
            return normalizeIndex(index);
        } catch (EmptyTracksDirException e) {
            throw new RuntimeException(e);
        }
    }

    private int firstTrackIndex() throws EmptyTracksDirException {
        try (Stream<Path> trackDirs = Files.list(ResourceUtils.tracksDirPath())) {
            List<Path> dirs = trackDirs.toList();

            if (dirs.isEmpty()) {
                throw new EmptyTracksDirException("Tracks directory is empty");
            }

            int latestIndex = 0;
            FileTime latestTime = Files.getLastModifiedTime(dirs.getFirst());

            for (int i = 1; i < dirs.size(); i++) {
                FileTime currentTime = Files.getLastModifiedTime(dirs.get(i));
                if (currentTime.compareTo(latestTime) > 0) {
                    latestTime = currentTime;
                    latestIndex = i;
                }
            }

            return latestIndex;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<String, String> titleAndArtist(Path trackDirPath) {
        String title;
        String artist;
        String separator = "\\^";

        String fileNameWithoutExtension = trackDirPath.getFileName().toString();
        String[] parts = fileNameWithoutExtension.split(separator);
        if (parts.length >= 2) {
            artist = parts[0];
            title = parts[1];
        } else if (parts.length == 1) {
            artist = "Unknown";
            title = parts[0];
        } else {
            artist = "Unknown";
            title = "Unknown";
        }

        return new Pair<>(title, artist);
    }
}
