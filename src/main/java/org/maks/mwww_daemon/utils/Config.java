package org.maks.mwww_daemon.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.maks.mwww_daemon.enumeration.FifoCommand;
import org.maks.mwww_daemon.fifo.FifoCommandQueue;
import org.maks.mwww_daemon.fifo.FifoCommandSubscriber;

import java.io.IOException;

public class Config implements FifoCommandSubscriber {

    public Config(FifoCommandQueue queue) {
         new Thread(() -> applyConfig(queue)).start();
    }

    @Override
    public void accept(FifoCommandQueue observable, FifoCommand fifoCommand) {
        if (fifoCommand == FifoCommand.RELOAD_CONFIG) {
            applyConfig(observable);
        }
    }

    private void applyConfig(FifoCommandQueue queue) {
        YAMLConfig config = loadYamlConfig();

        FifoCommand setSong = FifoCommand.build(FifoCommand.SET_SONG, config.playlist.initialSong);
        FifoCommand setSkipDuration = FifoCommand.build(FifoCommand.SET_SKIP_DURATION, config.audio.skipSeconds);

        queue.push(setSong);
        queue.push(setSkipDuration);
    }

    private YAMLConfig loadYamlConfig() {
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
            Playlist playlist,
            Audio audio
    ) {}

    private record Playlist(
            String initialSong
    ) {}

    private record Audio(
            int skipSeconds
    ) {}
}
