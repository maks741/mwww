package com.maks.mwww.backend.cmd;

import java.util.List;

public interface CmdOutputTransform<T> {

    T transform(List<String> list);

}
