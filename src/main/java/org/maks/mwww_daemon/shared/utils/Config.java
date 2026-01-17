package org.maks.mwww_daemon.shared.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

public class Config {

    private static final YAMLConfig config = loadYamlConfig();

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

    private static YAMLConfig loadYamlConfig() {
        var mapper = new ObjectMapper(new YAMLFactory());

        YAMLConfig yamlConfig;
        try {
            yamlConfig = mapper.readValue(ResourceUtils.configFilePath().toFile(), YAMLConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return yamlConfig;
    }

    private record YAMLConfig(
            SpotifyConfig spotify
    ) {}

    private record SpotifyConfig(
            String openOnStartupUri,
            String playlistId,
            String clientId,
            String redirectUri
    ) {}
}
