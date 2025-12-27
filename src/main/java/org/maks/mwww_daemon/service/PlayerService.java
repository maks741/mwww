package org.maks.mwww_daemon.service;

import javafx.scene.image.ImageView;
import org.maks.mwww_daemon.model.TempSongInfo;

import java.util.function.Consumer;

public interface PlayerService {

    void loadFirstSong();

    void play();

    void switchSong(String songIdentifier);

    void next();

    void previous();

    void skipForward();

    void skipBackward();

    void toggleOnRepeat();

    void togglePause();

    void addSong(ImageView addIcon);

    void deleteSong();

    void setOnSongUpdated(Consumer<TempSongInfo> consumer);

    boolean isPlaying();

    void setSkipDuration(int skipDuration);

}
