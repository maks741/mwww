package org.maks.musicplayer.service;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class DownloadService {

    public CompletableFuture<String> downloadSongByUrl() {
        String urlFromClipboard;
        try {
            urlFromClipboard = (String) Toolkit
                    .getDefaultToolkit()
                    .getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new RuntimeException(e);
        }

        return downloadSongByUrl(urlFromClipboard);
    }

    private CompletableFuture<String> downloadSongByUrl(String url) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        new Thread(() -> {
            Process downloadScriptProcess = executeDownloadScript(url);

            String newSongFolderName = readDownloadScriptOutput(downloadScriptProcess);
            completableFuture.complete(newSongFolderName);
        }).start();

        return completableFuture;
    }

    private String readDownloadScriptOutput(Process downloadScriptProcess) {
        String successfulOutputPrefix = "dirname";
        BufferedReader stdInput = new BufferedReader(
                new InputStreamReader(downloadScriptProcess.getInputStream())
        );

        String line;
        try {
            while ((line = stdInput.readLine()) != null) {
                if (line.startsWith(successfulOutputPrefix)) {
                    return line.split(":")[1];
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "";
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
