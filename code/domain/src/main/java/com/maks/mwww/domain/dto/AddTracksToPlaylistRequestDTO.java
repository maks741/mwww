package com.maks.mwww.domain.dto;

import java.util.List;

public record AddTracksToPlaylistRequestDTO(List<String> uris, int position) {
}
