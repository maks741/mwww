package org.maks.mwww_daemon.backend.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrackIndexProvider {

    private final LocalPlaylistUtils playlistUtils = new LocalPlaylistUtils();
    private final Random random = new Random();

    private final List<Integer> trackIndexes = new ArrayList<>();
    private int trackIndexesPointer = 0;

    private boolean shuffleOn = false;

    public void set(int index) {
        if (!shuffleOn) {
            clearExcept(index);
            return;
        }

        if (trackIndexes.contains(index)) {
            trackIndexesPointer = trackIndexes.indexOf(index);
        } else {
            trackIndexes.add(index);
            trackIndexesPointer = trackIndexes.size() - 1;
        }
    }

    public int next() {
        if (trackIndexesPointer == trackIndexes.size() - 1) {
            trackIndexes.add(newIndex());
        }

        return trackIndexes.get(++trackIndexesPointer);
    }

    public int previous() {
        if (shuffleOn) {
            trackIndexesPointer = Math.max(0, --trackIndexesPointer);
        } else {
            if (trackIndexesPointer == 0) {
                trackIndexes.addFirst(trackIndexes.getFirst() - 1);
            } else {
                --trackIndexesPointer;
            }
        }

        return current();
    }

    public int current() {
        return trackIndexes.get(trackIndexesPointer);
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
        trackIndexes.clear();
        trackIndexes.add(index);
        trackIndexesPointer = 0;
    }
}
