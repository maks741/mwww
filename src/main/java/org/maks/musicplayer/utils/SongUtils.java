package org.maks.musicplayer.utils;

import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.model.SongPlayer;
import org.maks.musicplayer.model.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SongUtils {

    private static final List<Song> SONG_LIST = loadMusicList();

    private static List<Song> loadMusicList() {
        File songsFolder = new File("./songs");
        File[] songFolders = songsFolder.listFiles();

        if (songFolders == null) {
            throw new RuntimeException("Songs directory is non-existent");
        }

        return new ArrayList<>(Arrays.stream(songFolders)
                .sorted(Comparator.comparing(File::getName))
                .filter(File::isDirectory)
                .map(songFolder -> {
                    SongPlayer songPlayer = new SongPlayer(songFolder);
                    SongInfo songInfo = new SongInfo();
                    songInfo.load(songPlayer);

                    return new Song(songInfo, songPlayer);
                })
                .toList());
    }

    public static List<Song> musicList() {
        return SONG_LIST;
    }

}
