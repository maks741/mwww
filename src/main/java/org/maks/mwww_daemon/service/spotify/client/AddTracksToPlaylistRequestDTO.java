package org.maks.mwww_daemon.service.spotify.client;

import java.util.List;

public record AddTracksToPlaylistRequestDTO(List<String> uris, int position) {
}
