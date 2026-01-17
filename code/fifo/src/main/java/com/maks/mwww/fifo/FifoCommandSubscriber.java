package com.maks.mwww.fifo;

import com.maks.mwww.domain.enumeration.FifoCommand;

public interface FifoCommandSubscriber {

    void accept(FifoCommandQueue queue, FifoCommand fifoCommand);

}
