package org.maks.musicplayer.service;

import javafx.concurrent.Task;
import javafx.scene.input.Clipboard;

import java.io.IOException;

public class DownloadService {

    public Task<Void> downloadSong() {
        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasString()) {
            throw new RuntimeException("Clipboard has no content");
        }

        String urlFromClipboard = clipboard.getString();
        return downloadSongByUrl(urlFromClipboard);
    }

    private Task<Void> downloadSongByUrl(String url) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Process downloadScriptProcess = executeDownloadScript(url);
                downloadScriptProcess.waitFor();
                return null;
            }
        };

        new Thread(task).start();

        return task;
    }

    private Process executeDownloadScript(String url) {
        String youtubeDownloadServiceBasePath = "./youtube-download-service/";
        String[] commands = {
                youtubeDownloadServiceBasePath + ".venv/bin/python",
                youtubeDownloadServiceBasePath + "main.py",
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
