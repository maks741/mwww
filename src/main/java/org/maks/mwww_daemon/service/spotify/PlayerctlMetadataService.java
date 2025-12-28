package org.maks.mwww_daemon.service.spotify;

import org.maks.mwww_daemon.model.PlayerctlMetadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerctlMetadataService {

    private final CmdService cmdService = new CmdService();
    private final Consumer<PlayerctlMetadata> metadataConsumer;

    private static final Pattern METADATA_LINE = Pattern.compile("^spotifyd\\s+(\\S+)\\s+(.*)$");
    private static final Pattern TRACK_ID_LINE = Pattern.compile("^'/(\\w+)/(\\w+)/(\\w+)'$");

    public PlayerctlMetadataService(Consumer<PlayerctlMetadata> metadataConsumer) {
        this.metadataConsumer = metadataConsumer;
        new Thread(this::listenToMetadataUpdates).start();
    }

    private void listenToMetadataUpdates() {
        var processBuilder = new ProcessBuilder("playerctl", "-p", "spotifyd", "metadata", "mpris:trackid", "--follow");

        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("'/org/mpris/MediaPlayer2/TrackList/NoTrack'")) {
                        continue;
                    }

                    // whenever trackid is updated, read full metadata and call accept
                    metadataConsumer.accept(readFullMetadata());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PlayerctlMetadata readFullMetadata() {
        List<String> metadataLines = cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "metadata");

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

    private static String processTrackId(String trackId) {
        Matcher matcher = TRACK_ID_LINE.matcher(trackId);
        if (!matcher.matches()) {
            throw new RuntimeException("Unexpected trackid value: " + trackId);
        }

        String separator = ":";
        return matcher.group(1) + separator + matcher.group(2) + separator + matcher.group(3);
    }
}
