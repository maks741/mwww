package org.maks.musicplayer.fifo;

import org.maks.musicplayer.enumeration.FifoCommand;

public interface FifoCommandSubscriber {

    void accept(FifoCommand fifoCommand);

}
