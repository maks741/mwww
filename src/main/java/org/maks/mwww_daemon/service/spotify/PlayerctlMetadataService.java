package org.maks.mwww_daemon.service.spotify;

import org.maks.mwww_daemon.exception.PlayerctlNoTrackException;
import org.maks.mwww_daemon.model.PlayerctlMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerctlMetadataService extends PlayerctlFollowService<PlayerctlMetadata> {

    private static final Logger LOG = Logger.getLogger(PlayerctlMetadataService.class.getName());

    private static final Pattern METADATA_LINE = Pattern.compile("^spotifyd\\s+(\\S+)\\s+(.*)$");
    private static final Pattern TRACK_ID_LINE = Pattern.compile("^'/(\\w+)/(\\w+)/(\\w+)'$");

    @Override
    protected PlayerctlMetadata accept(String trackId) {
        // whenever trackid is updated, read full metadata
        PlayerctlMetadata metadata;
        try {
            metadata = readFullMetadata();
        } catch (PlayerctlNoTrackException _) {
            LOG.warning("Ignoring playerctl NoTrack metadata");
            metadata = null;
        }

        return metadata;
    }

    @Override
    protected List<String> fields() {
        return List.of("metadata", "mpris:trackid");
    }

    public PlayerctlMetadata readFullMetadata() throws PlayerctlNoTrackException {
        List<String> metadataLines = cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "metadata");

        // ignore if updated to NoTrack
        if (metadataLines.size() == 1 && metadataLines.getFirst().contains("/org/mpris/MediaPlayer2/TrackList/NoTrack")) {
            throw new PlayerctlNoTrackException();
        }

        String trackId = null;
        String title = null;
        String artUrl = null;
        String album = null;
        long length = 0;

        List<String> artists = new ArrayList<>();
        List<String> albumArtists = new ArrayList<>();

        for (String line : metadataLines) {
            // example: spotifyd xesam:title               STELLAR
            Matcher matcher = METADATA_LINE.matcher(line);
            if (!matcher.matches()) {
                throw new RuntimeException("Unexpected line in playerctl metadata output: " + line);
            }

            String key = matcher.group(1);
            String value = matcher.group(2);

            switch (key) {
                case "mpris:trackid" -> trackId = processTrackId(value);
                case "xesam:title" -> title = value;
                case "mpris:artUrl" -> artUrl = value;
                case "xesam:album" -> album = value;

                case "xesam:artist" -> artists.add(value);
                case "xesam:albumArtist" -> albumArtists.add(value);

                case "mpris:length" -> {
                    try {
                        length = Long.parseLong(value);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return new PlayerctlMetadata(
                trackId,
                title,
                List.copyOf(artists),
                artUrl,
                album,
                List.copyOf(albumArtists),
                length
        );
    }

    private String processTrackId(String trackId) {
        Matcher matcher = TRACK_ID_LINE.matcher(trackId);
        if (!matcher.matches()) {
            throw new RuntimeException("Unexpected trackid value: " + trackId);
        }

        String separator = ":";
        return matcher.group(1) + separator + matcher.group(2) + separator + matcher.group(3);
    }
}
