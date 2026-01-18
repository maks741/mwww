package com.maks.mwww.fifo;

import com.maks.mwww.domain.enumeration.FifoCommand;

import java.util.ArrayList;
import java.util.List;

public class FifoCommandQueue {

    private final List<FifoCommandSubscriber> subscribers = new ArrayList<>();

    private static FifoCommandQueue instance;

    private FifoCommandQueue() {}

    public static FifoCommandQueue instance() {
        if (instance == null) {
            instance = new FifoCommandQueue();
            FifoService.listenToFifoCommands(instance);
        }
        return instance;
    }

    public void subscribe(FifoCommandSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void push(FifoCommand fifoCommand) {
        for (var subscriber : subscribers) {
            subscriber.accept(this, fifoCommand);
        }
    }
}
