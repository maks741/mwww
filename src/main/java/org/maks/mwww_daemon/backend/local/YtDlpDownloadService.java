package org.maks.mwww_daemon.backend.local;

import org.maks.mwww_daemon.backend.service.AsyncRunnerService;
import org.maks.mwww_daemon.backend.cmd.CmdService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class YtDlpDownloadService {

    private final AsyncRunnerService asyncRunnerService = new AsyncRunnerService();
    private final CmdService cmdService = new CmdService();

    public CompletableFuture<String> downloadTrack(String url) {
        return asyncRunnerService.run(() -> {
            if (url.trim().isEmpty()) {
                throw new RuntimeException("URL cannot be empty");
            }

            if (!url.startsWith("https://")) {
                throw new RuntimeException("Invalid URL");
            }

            final Map<String, String> downloadedTrackUrlMap = new HashMap<>();
            String targetLogPrefix = "Downloaded track name: ";
            String downloadedTrackUrlKey = "downloadedTrackUrl";

            cmdService.runCmdCommand(line -> {
                if (line.startsWith(targetLogPrefix)) {
                    downloadedTrackUrlMap.put(downloadedTrackUrlKey, line.substring(targetLogPrefix.length()));
                }
            },"mwww-youtube-download", url);

            if (!downloadedTrackUrlMap.containsKey(downloadedTrackUrlKey)) {
                throw new RuntimeException("Could not download track by url: " + url);
            }

            return downloadedTrackUrlMap.get(downloadedTrackUrlKey);
        });
    }
}
