package org.maks.musicplayer.utils;

import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.model.Song;
import org.maks.musicplayer.model.SongPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaylistUtils {

    private static final List<Song> playlist = new ArrayList<>();

    static {
        loadPlaylist();
    }

    private static void loadPlaylist() {
        File songsFolder = new File("./songs");
        File[] songFolders = songsFolder.listFiles();

        if (songFolders == null) {
            throw new RuntimeException("Songs directory is non-existent");
        }

        playlist.addAll(Arrays.stream(songFolders)
                .filter(File::isDirectory)
                .map(songFolder -> {
                    SongPlayer songPlayer = new SongPlayer(songFolder);
                    SongInfo songInfo = new SongInfo();
                    songInfo.load(songPlayer);

                    return new Song(songInfo, songPlayer);
                })
                .toList());
    }

    public static void refresh() {
        playlist.clear();
        loadPlaylist();
    }

    public static List<Song> playlist() {
        return playlist;
    }

}
