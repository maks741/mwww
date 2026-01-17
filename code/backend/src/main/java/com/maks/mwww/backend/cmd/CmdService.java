package com.maks.mwww.backend.cmd;

import com.maks.mwww.domain.exception.CmdServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class CmdService {

    private static final Logger LOG = Logger.getLogger(CmdService.class.getName());

    public <T> T runCmdCommand(CmdOutputTransform<T> cmdOutputTransform, String... commands) throws CmdServiceException {
        return cmdOutputTransform.transform(runCmdCommand(commands));
    }

    public void runCmdCommand(Consumer<String> lineConsumer, String... commands) throws CmdServiceException {
        var processBuilder = new ProcessBuilder(commands);

        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineConsumer.accept(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            waitForProcessExit(process, commands);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> runCmdCommand(String... commands) throws CmdServiceException {
        var processBuilder = new ProcessBuilder(commands);
        List<String> lines;

        try {
            Process process = processBuilder.start();

            lines = readOutput(new InputStreamReader(process.getInputStream()));

            waitForProcessExit(process, commands);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return lines;
    }

    private void waitForProcessExit(Process process, String... commands) throws InterruptedException {
        int exitCode = process.waitFor();
        LOG.info("Exit code for command '" + String.join(" ", commands) + "': " + exitCode);
        if (exitCode != 0) {
            var command = String.join(" ", commands);
            List<String> errorOutput = readOutput(new InputStreamReader(process.getErrorStream()));
            String errorOutputStr = new StringCmdOutputTransform().transform(errorOutput);

            throw new CmdServiceException(
                    "Command " + command + " did not finish successfully.\nOutput:\n" + errorOutputStr,
                    errorOutputStr
            );
        }
    }

    private List<String> readOutput(InputStreamReader inputStreamReader) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return lines;
    }
}
