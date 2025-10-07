package org.maks.mwww_daemon.service;

import javafx.concurrent.Task;
import javafx.scene.input.Clipboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DownloadService {

    public Task<String> downloadSong() {
        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasString()) {
            throw new RuntimeException("Clipboard has no content");
        }

        String urlFromClipboard = clipboard.getString();
        return downloadSongByUrl(urlFromClipboard);
    }

    private Task<String> downloadSongByUrl(String url) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                Process downloadScriptProcess = executeDownloadScript(url);
                String targetLogPrefix = "Downloaded song name: ";

                try (InputStreamReader inputStreamReader = new InputStreamReader(downloadScriptProcess.getInputStream());
                     BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (!line.startsWith(targetLogPrefix)) {
                            continue;
                        }

                        return line.split(targetLogPrefix)[1];
                    }
                }

                throw new RuntimeException("Could not download song by url: " + url);
            }
        };

        new Thread(task).start();

        return task;
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
