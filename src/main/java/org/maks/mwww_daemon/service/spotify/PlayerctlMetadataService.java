package org.maks.mwww_daemon.service.spotify;

import org.maks.mwww_daemon.exception.PlayerctlNoTrackException;
import org.maks.mwww_daemon.model.PlayerctlMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerctlMetadataService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final CmdService cmdService = new CmdService();
    private final List<Consumer<PlayerctlMetadata>> metadataConsumers = new ArrayList<>();

    private Future<?> playerctlMetadataTask;

    private static final Pattern METADATA_LINE = Pattern.compile("^spotifyd\\s+(\\S+)\\s+(.*)$");
    private static final Pattern TRACK_ID_LINE = Pattern.compile("^'/(\\w+)/(\\w+)/(\\w+)'$");

    public void listen(Consumer<PlayerctlMetadata> consumer) {
        if (playerctlMetadataTask != null) {
            playerctlMetadataTask.cancel(true);
        }

        metadataConsumers.add(consumer);
        playerctlMetadataTask = executorService.submit(this::listenToMetadataUpdates);
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

    public void shutdown() {
        if (playerctlMetadataTask != null) {
            playerctlMetadataTask.cancel(true);
        }
        metadataConsumers.clear();
        executorService.shutdown();
    }

    private void listenToMetadataUpdates() {
        // whenever trackid is updated, read full metadata and call accept
        cmdService.runCmdCommand(_ -> {
            PlayerctlMetadata metadata;
            try {
                metadata = readFullMetadata();
            } catch (PlayerctlNoTrackException _) {
                return;
            }

            metadataConsumers.forEach(consumer -> consumer.accept(metadata));
        }, "playerctl", "-p", "spotifyd", "metadata", "mpris:trackid", "--follow");
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
