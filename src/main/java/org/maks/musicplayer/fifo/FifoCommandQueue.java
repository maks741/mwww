package org.maks.musicplayer.fifo;

import javafx.application.Platform;
import org.maks.musicplayer.enumeration.FifoCommand;

import java.util.ArrayList;
import java.util.List;

public class FifoCommandQueue {

    private final List<FifoCommandSubscriber> subscribers = new ArrayList<>();

    public void subscribe(FifoCommandSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void push(FifoCommand fifoCommand) {
        for (var subscriber : subscribers) {
            Platform.runLater(() -> subscriber.accept(fifoCommand));
        }
    }
}
