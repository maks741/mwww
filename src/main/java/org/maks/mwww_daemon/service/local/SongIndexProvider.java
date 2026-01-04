package org.maks.mwww_daemon.service.local;

public class SongIndexProvider {

    private int currentSongIndex = 0;

    public void set(int index) {
        currentSongIndex = index;
    }

    public int next() {
        return ++currentSongIndex;
    }

    public int previous() {
        return --currentSongIndex;
    }

    public int current() {
        return currentSongIndex;
    }
}
