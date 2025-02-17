package org.maks.musicplayer.utils;

import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.model.Song;
import org.maks.musicplayer.model.SongInfoDto;
import org.maks.musicplayer.model.SongPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaylistUtils {

    public static List<SongInfo> songInfoList() {
        File[] songFolders = songFolders();
        SongUtils songUtils = new SongUtils();

        return Arrays.stream(songFolders)
                .filter(File::isDirectory)
                .map(songFolder -> {
                    SongInfoDto songInfoDto = songUtils.songInfoDto(songFolder);

                    SongInfo songInfo = new SongInfo();
                    songInfo.load(songInfoDto);

                    return songInfo;
                })
                .toList();
    }

    public static Song songByIndex(int index) {
        File[] songFolders = songFolders();
        File songFolder = songFolders[index];

        SongPlayer songPlayer = new SongPlayer(songFolder);
        SongInfo songInfo = new SongInfo();
        songInfo.load(songPlayer.songInfoDto());

        return new Song(songInfo, songPlayer);
    }

    public static void refresh() {
        // TODO
    }

    public static int amountOfSongs() {
        return songFolders().length;
    }

    private static File[] songFolders() {
        File songsFolder = new File("./songs");
        File[] songFolders = songsFolder.listFiles();

        if (songFolders == null) {
            throw new RuntimeException("Songs directory is non-existent");
        }

        return songFolders;
    }

}
