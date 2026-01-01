package org.maks.mwww_daemon.service.spotify.cmdoutputtransform;

import java.util.List;

public interface CmdOutputTransform<T> {

    T transform(List<String> list);

}
