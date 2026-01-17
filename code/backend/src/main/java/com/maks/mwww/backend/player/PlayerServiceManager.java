package com.maks.mwww.backend.player;

import com.maks.mwww.backend.local.LocalPlayerService;
import com.maks.mwww.backend.spotify.SpotifyPlayerService;
import com.maks.mwww.cqrs.BackendToUIBridge;
import com.maks.mwww.domain.enumeration.PlayerContext;

public class PlayerServiceManager {

    public PlayerService<?> getPlayerService(BackendToUIBridge bridge) {
        return new SpotifyPlayerService(bridge);
    }

    public PlayerService<?> getPlayerService(String context, BackendToUIBridge bridge) {
        if (context.equals(PlayerContext.LOCAL.context())) {
            return new LocalPlayerService(bridge);
        } else if (context.equals(PlayerContext.SPOTIFY.context())) {
            return new SpotifyPlayerService(bridge);
        }

        return null;
    }
}
