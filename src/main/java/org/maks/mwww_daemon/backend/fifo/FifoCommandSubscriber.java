package org.maks.mwww_daemon.backend.fifo;

import org.maks.mwww_daemon.shared.domain.enumeration.FifoCommand;

public interface FifoCommandSubscriber {

    void accept(FifoCommandQueue observable, FifoCommand fifoCommand);

}
