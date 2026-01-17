package com.maks.mwww.backend.cmd;

import java.util.List;

public class StringCmdOutputTransform implements CmdOutputTransform<String> {

    @Override
    public String transform(List<String> list) {
        return String.join("\n", list);
    }
}
