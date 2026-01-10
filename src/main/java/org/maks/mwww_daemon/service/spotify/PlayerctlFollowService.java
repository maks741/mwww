package org.maks.mwww_daemon.service.spotify;

import org.maks.mwww_daemon.exception.CmdServiceException;
import org.maks.mwww_daemon.service.spotify.cmdoutputtransform.StringCmdOutputTransform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class PlayerctlFollowService<T> {

    private static final Logger LOG = Logger.getLogger(PlayerctlFollowService.class.getName());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    protected final CmdService cmdService = new CmdService();
    private final List<Consumer<T>> metadataConsumers = new ArrayList<>();

    private final List<String> commands = new ArrayList<>(List.of("playerctl", "-p", "spotifyd"));

    private Future<?> playerctlMetadataTask;

    public PlayerctlFollowService() {
        commands.addAll(fields());
    }

    public void listen(Consumer<T> consumer) {
        if (playerctlMetadataTask != null) {
            playerctlMetadataTask.cancel(true);
        }

        metadataConsumers.add(consumer);
        playerctlMetadataTask = executorService.submit(this::follow);

        try {
            T initialValue = this.accept(
                    cmdService.runCmdCommand(new StringCmdOutputTransform(), toArray(commands))
            );

            if (initialValue == null) {
                return;
            }

            consumer.accept(initialValue);
        } catch (CmdServiceException e) {
            LOG.warning("Could not set initial value. Error: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (playerctlMetadataTask != null) {
            playerctlMetadataTask.cancel(true);
        }
        metadataConsumers.clear();
        executorService.shutdown();
    }

    public void restartTask() {
        if (playerctlMetadataTask != null) {
            playerctlMetadataTask.cancel(true);
        }
        playerctlMetadataTask = executorService.submit(this::follow);
    }

    private void follow() {
        List<String> followCommands = new ArrayList<>(commands);
        followCommands.add("--follow");

        cmdService.runCmdCommand(line -> {
            T output = this.accept(line);

            if (output == null) {
                return;
            }

            metadataConsumers.forEach(consumer -> consumer.accept(output));
        }, toArray(followCommands));
    }

    protected abstract T accept(String line);

    protected abstract List<String> fields();

    private String[] toArray(List<String> list) {
        return list.toArray(new String[0]);
    }

}
