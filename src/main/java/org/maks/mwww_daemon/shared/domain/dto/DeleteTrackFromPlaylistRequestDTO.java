package org.maks.mwww_daemon.shared.domain.dto;

import java.util.List;

public record DeleteTrackFromPlaylistRequestDTO(List<DeleteTrackFromPlaylistItemRequestDto> tracks) {
    public record DeleteTrackFromPlaylistItemRequestDto(String uri) {}
}
