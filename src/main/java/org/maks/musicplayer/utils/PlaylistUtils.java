package org.maks.musicplayer.utils;

import org.maks.musicplayer.components.SongInfo;
import org.maks.musicplayer.model.Song;
import org.maks.musicplayer.model.SongInfoDto;
import org.maks.musicplayer.model.SongPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class PlaylistUtils {

    public List<SongInfo> songInfoList() {
        SongUtils songUtils = new SongUtils();

        return listSongFolderPaths()
                .filter(Files::isDirectory)
                .map(songFolderPath -> {
                    SongInfoDto songInfoDto = songUtils.songInfoDto(songFolderPath);

                    SongInfo songInfo = new SongInfo();
                    songInfo.load(songInfoDto);

                    return songInfo;
                })
                .toList();
    }

    public Song songByIndex(int index) {
        Path songFolderPath = listSongFolderPaths()
                .skip(index)
                .findFirst()
                .orElseThrow();

        SongPlayer songPlayer = new SongPlayer(songFolderPath);
        SongInfo songInfo = new SongInfo();
        songInfo.load(songPlayer.songInfoDto());

        return new Song(songInfo, songPlayer);
    }

    public int amountOfSongs() {
        return (int) listSongFolderPaths().count();
    }

    private Stream<Path> listSongFolderPaths() {
        Path root = ResourceUtils.songsFoldersRoot();

        try {
            return Files.list(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
