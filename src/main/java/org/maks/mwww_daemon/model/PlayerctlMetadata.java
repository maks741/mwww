package org.maks.mwww_daemon.model;

import java.util.List;

public record PlayerctlMetadata(
        String trackId,
        String title,
        List<String> artists,
        String artUrl,
        String album,
        List<String> albumArtists,
        long length
) {
}
