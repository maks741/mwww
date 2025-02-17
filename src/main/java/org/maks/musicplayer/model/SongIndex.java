package org.maks.musicplayer.model;

public class SongIndex {

    private int value = 0;

    public void set(int value) {
        this.value = value;
    }

    public void next() {
        value++;
    }

    public void previous() {
        value--;
    }

    public int get() {
        return value;
    }

}
