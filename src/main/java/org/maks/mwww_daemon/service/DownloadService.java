package org.maks.mwww_daemon.service;

import javafx.scene.input.Clipboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    public CompletableFuture<String> downloadSong() {
        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasString()) {
            throw new RuntimeException("Clipboard has no content");
        }

        String urlFromClipboard = clipboard.getString();
        return downloadSongByUrl(urlFromClipboard);
    }

    private CompletableFuture<String> downloadSongByUrl(String url) {
        return CompletableFuture.supplyAsync(() -> {
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
        }, executorService);
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
