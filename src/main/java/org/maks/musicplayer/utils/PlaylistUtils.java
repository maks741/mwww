package org.maks.musicplayer.utils;

import org.maks.musicplayer.model.SongInfo;
import org.maks.musicplayer.model.SongPlayer;

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

            return new SongUtils().songInfo(songFolderPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SongPlayer audio(int index) {
        try (Stream<Path> songDirs = Files.list(Paths.get("songs"))) {
            Path songFolderPath = songDirs
                    .skip(index)
                    .findFirst()
                    .orElseThrow();

            return new SongPlayer(songFolderPath);
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
}
