package org.maks.mwww_daemon.backend.cmd;

import java.util.List;

public interface CmdOutputTransform<T> {

    T transform(List<String> list);

}
