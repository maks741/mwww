package com.maks.mwww.domain.dto;

import java.util.List;

public record DeleteTrackFromPlaylistRequestDTO(List<DeleteTrackFromPlaylistItemRequestDto> tracks) {
    public record DeleteTrackFromPlaylistItemRequestDto(String uri) {}
}
