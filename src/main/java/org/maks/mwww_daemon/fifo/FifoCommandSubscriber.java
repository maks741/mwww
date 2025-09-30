package org.maks.mwww_daemon.fifo;

import org.maks.mwww_daemon.enumeration.FifoCommand;

public interface FifoCommandSubscriber {

    void accept(FifoCommand fifoCommand);

}
