package org.maks.mwww_daemon.service.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SongIndexProvider {

    private final LocalPlaylistUtils playlistUtils = new LocalPlaylistUtils();
    private final Random random = new Random();

    private final List<Integer> songIndexes = new ArrayList<>();
    private int songIndexesPointer = 0;

    private boolean shuffleOn = false;

    public void set(int index) {
        if (!shuffleOn) {
            clearExcept(index);
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
            songIndexes.add(newIndex());
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

    public void toggleShuffle() {
        int currentIndex = current();
        clearExcept(currentIndex);

        shuffleOn = !shuffleOn;
    }

    private int newIndex() {
        int newIndex;
        if (shuffleOn) {
            newIndex = random.nextInt(playlistUtils.count());
        } else {
            newIndex = current() + 1;
        }

        return newIndex;
    }

    private void clearExcept(int index) {
        songIndexes.clear();
        songIndexes.add(index);
        songIndexesPointer = 0;
    }
}
