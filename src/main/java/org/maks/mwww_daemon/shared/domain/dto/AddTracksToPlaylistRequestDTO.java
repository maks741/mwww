package org.maks.mwww_daemon.shared.domain.dto;

import java.util.List;

public record AddTracksToPlaylistRequestDTO(List<String> uris, int position) {
}
