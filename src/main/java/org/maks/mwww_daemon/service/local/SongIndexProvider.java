package org.maks.mwww_daemon.service.local;

import java.util.ArrayList;
import java.util.List;

public class SongIndexProvider {

    private final List<Integer> songIndexes = new ArrayList<>();
    private int songIndexesPointer = 0;

    private boolean shuffleOn = false;

    public void set(int index) {
        if (!shuffleOn) {
            songIndexes.clear();
            songIndexes.add(index);
            songIndexesPointer = 0;
            return;
        }

        if (songIndexes.contains(index)) {
            songIndexesPointer = songIndexes.indexOf(index);
        } else {
            songIndexes.add(index);
            songIndexesPointer = songIndexes.size() - 1;
        }
    }

    public int next() {
        if (songIndexesPointer == songIndexes.size() - 1) {
            songIndexes.add(newIndexForward());
        }

        return songIndexes.get(++songIndexesPointer);
    }

    public int previous() {
        if (shuffleOn) {
            songIndexesPointer = Math.max(0, --songIndexesPointer);
        } else {
            if (songIndexesPointer == 0) {
                songIndexes.addFirst(songIndexes.getFirst() - 1);
            } else {
                --songIndexesPointer;
            }
        }

        return current();
    }

    public int current() {
        return songIndexes.get(songIndexesPointer);
    }

    public int first() {
        return 0;
    }

    private int newIndexForward() {
        // TODO: generate random when shuffle is on
        return current() + 1;
    }
}
