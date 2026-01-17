package com.maks.mwww.backend.spotify;

import java.util.List;

public class PlayerctlStatusService extends PlayerctlFollowService<String> {

    @Override
    protected String accept(String line) {
        return line;
    }

    @Override
    protected List<String> fields() {
        return List.of("status");
    }
}
