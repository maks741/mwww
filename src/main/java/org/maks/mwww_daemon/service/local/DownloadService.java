package org.maks.mwww_daemon.service.local;

import org.maks.mwww_daemon.service.AsyncRunnerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class DownloadService {

    private final AsyncRunnerService asyncRunnerService = new AsyncRunnerService();

    public CompletableFuture<String> downloadSong(String url) {
        return asyncRunnerService.run(() -> {
            if (url.trim().isEmpty()) {
                throw new RuntimeException("URL cannot be empty");
            }

            if (!url.startsWith("https://")) {
                throw new RuntimeException("Invalid URL");
            }

            Process downloadScriptProcess = executeDownloadScript(url);
            String targetLogPrefix = "Downloaded song name: ";

            try (InputStreamReader inputStreamReader = new InputStreamReader(downloadScriptProcess.getInputStream());
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith(targetLogPrefix)) {
                        return line.substring(targetLogPrefix.length());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            throw new RuntimeException("Could not download song by url: " + url);
        });
    }

    private Process executeDownloadScript(String url) {
        String[] commands = {
                "mwww-youtube-download",
                url
        };

        Process process;
        try {
            process = Runtime.getRuntime().exec(commands);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return process;
    }
}
