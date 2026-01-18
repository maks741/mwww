package com.maks.mwww.backend.player;

import com.maks.mwww.backend.local.LocalPlayerService;
import com.maks.mwww.backend.spotify.SpotifyPlayerService;
import com.maks.mwww.domain.enumeration.PlayerContext;

public class PlayerServiceManager {

    public PlayerService<?> getPlayerService() {
        return new SpotifyPlayerService();
    }

    public PlayerService<?> getPlayerService(String context) {
        if (context.equals(PlayerContext.LOCAL.context())) {
            return new LocalPlayerService();
        } else if (context.equals(PlayerContext.SPOTIFY.context())) {
            return new SpotifyPlayerService();
        }

        return null;
    }
}
