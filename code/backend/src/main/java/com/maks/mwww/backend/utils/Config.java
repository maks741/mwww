package com.maks.mwww.backend.utils;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;

public class Config {

    private static final JSONConfig config = loadConfig();

    public static String spotifyOpenOnStartupUri() {
        return config.spotify.openOnStartupUri;
    }

    public static String spotifyPlaylistId() {
        return config.spotify.playlistId;
    }

    public static String spotifyClientId() {
        return config.spotify.clientId;
    }

    public static String spotifyRedirectUri() {
        return config.spotify.redirectUri;
    }

    private static JSONConfig loadConfig() {
        try {
            String json = Files.readString(ResourceUtils.configFilePath());
            return new Gson().fromJson(json, JSONConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record JSONConfig(
            SpotifyConfig spotify
    ) {}

    private record SpotifyConfig(
            String openOnStartupUri,
            String playlistId,
            String clientId,
            String redirectUri
    ) {}
}
