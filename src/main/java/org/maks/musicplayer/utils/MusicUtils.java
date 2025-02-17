package org.maks.musicplayer.utils;

import javafx.scene.image.ImageView;
import org.maks.musicplayer.components.MusicInfo;
import org.maks.musicplayer.model.MediaPlayerContainer;
import org.maks.musicplayer.model.Music;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MusicUtils {

    private static final List<Music> musicList = loadMusicList();

    private static List<Music> loadMusicList() {
        File songsFolder = new File("./songs");
        File[] songFolders = songsFolder.listFiles();

        if (songFolders == null) {
            throw new RuntimeException("Songs directory is non-existent");
        }

        return new ArrayList<>(Arrays.stream(songFolders)
                .sorted(Comparator.comparing(File::getName))
                .map(songFolder -> {
                    MediaPlayerContainer mediaPlayerContainer = new MediaPlayerContainer(songFolder);
                    MusicInfo musicInfo = new MusicInfo();
                    musicInfo.load(mediaPlayerContainer);

                    return new Music(musicInfo, mediaPlayerContainer);
                })
                .toList());
    }

    public static List<Music> musicList() {
        return musicList;
    }

}
