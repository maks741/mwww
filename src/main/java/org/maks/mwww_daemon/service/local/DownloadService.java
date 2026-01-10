package org.maks.mwww_daemon.service.local;

import org.maks.mwww_daemon.service.AsyncRunnerService;
import org.maks.mwww_daemon.service.spotify.CmdService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DownloadService {

    private final AsyncRunnerService asyncRunnerService = new AsyncRunnerService();
    private final CmdService cmdService = new CmdService();

    public CompletableFuture<String> downloadSong(String url) {
        return asyncRunnerService.run(() -> {
            if (url.trim().isEmpty()) {
                throw new RuntimeException("URL cannot be empty");
            }

            if (!url.startsWith("https://")) {
                throw new RuntimeException("Invalid URL");
            }

            final Map<String, String> downloadedSongUrlMap = new HashMap<>();
            String targetLogPrefix = "Downloaded song name: ";
            String downloadedSongUrlKey = "downloadedSongUrl";

            cmdService.runCmdCommand(line -> {
                if (line.startsWith(targetLogPrefix)) {
                    downloadedSongUrlMap.put(downloadedSongUrlKey, line.substring(targetLogPrefix.length()));
                }
            },"mwww-youtube-download", url);

            if (!downloadedSongUrlMap.containsKey(downloadedSongUrlKey)) {
                throw new RuntimeException("Could not download song by url: " + url);
            }

            return downloadedSongUrlMap.get(downloadedSongUrlKey);
        });
    }
}
