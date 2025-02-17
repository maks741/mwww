package org.maks.musicplayer.utils;

import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.model.Song;
import org.maks.musicplayer.model.SongInfoDto;
import org.maks.musicplayer.model.SongPlayer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PlaylistUtils {

    public List<SongInfo> songInfoList() {
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

    public Song songByIndex(int index) {
        File[] songFolders = songFolders();
        File songFolder = songFolders[index];

        SongPlayer songPlayer = new SongPlayer(songFolder);
        SongInfo songInfo = new SongInfo();
        songInfo.load(songPlayer.songInfoDto());

        return new Song(songInfo, songPlayer);
    }

    public int amountOfSongs() {
        return songFolders().length;
    }

    private File[] songFolders() {
        File songsFolder = new File("./songs");

        if (!songsFolder.exists()) {
            boolean songsFolderCreated = songsFolder.mkdir();
            if (!songsFolderCreated) {
                throw new RuntimeException("Could not create songs directory");
            }
        }

        return songsFolder.listFiles();
    }

}
