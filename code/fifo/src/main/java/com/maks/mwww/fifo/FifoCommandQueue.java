package com.maks.mwww.fifo;

import com.maks.mwww.domain.enumeration.FifoCommand;

import java.util.ArrayList;
import java.util.List;

public class FifoCommandQueue {

    private final List<FifoCommandSubscriber> subscribers = new ArrayList<>();

    public void subscribe(FifoCommandSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void push(FifoCommand fifoCommand) {
        for (var subscriber : subscribers) {
            // TODO: ensure the consumers use Platform.runLater
            // Platform.runLater(() -> subscriber.accept(this, fifoCommand));
            subscriber.accept(this, fifoCommand);
        }
    }
}
