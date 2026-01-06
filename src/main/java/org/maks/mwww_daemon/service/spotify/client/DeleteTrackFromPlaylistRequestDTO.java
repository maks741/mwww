package org.maks.mwww_daemon.service.spotify.client;

import java.util.List;

public record DeleteTrackFromPlaylistRequestDTO(List<DeleteTrackFromPlaylistItemRequestDto> tracks) {
    public record DeleteTrackFromPlaylistItemRequestDto(String uri) {}
}
