package org.maks.mwww_daemon.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.maks.mwww_daemon.enumeration.FifoCommand;
import org.maks.mwww_daemon.fifo.FifoCommandQueue;
import org.maks.mwww_daemon.fifo.FifoCommandSubscriber;

import java.io.IOException;

public class Config implements FifoCommandSubscriber {

    private static final YAMLConfig config = loadYamlConfig();

    @Override
    public void accept(FifoCommandQueue observable, FifoCommand fifoCommand) {
        if (fifoCommand == FifoCommand.RELOAD_CONFIG) {
            applyConfig(observable);
        }
    }

    public static String initialSong() {
        return config.playlist.initialSong;
    }

    public static String spotifyClientId() {
        return config.spotify.clientId;
    }

    public static String spotifyRedirectUri() {
        return config.spotify.redirectUri;
    }

    private void applyConfig(FifoCommandQueue queue) {
        YAMLConfig config = loadYamlConfig();

        FifoCommand setSong = FifoCommand.build(FifoCommand.SET_SONG, config.playlist.initialSong);

        queue.push(setSong);
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
            PlaylistConfig playlist,
            SpotifyConfig spotify
    ) {}

    private record PlaylistConfig(
            String initialSong
    ) {}

    private record SpotifyConfig(
            String clientId,
            String redirectUri
    ) {}
}
