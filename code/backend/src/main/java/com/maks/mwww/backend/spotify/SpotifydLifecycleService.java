package com.maks.mwww.backend.spotify;

import com.maks.mwww.backend.cmd.CmdService;
import com.maks.mwww.domain.exception.CmdServiceException;
import com.maks.mwww.backend.cmd.StringCmdOutputTransform;
import com.maks.mwww.backend.utils.Config;
import com.maks.mwww.backend.utils.ResourceUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SpotifydLifecycleService {

    private final CmdService cmdService = new CmdService();
    private final double initialVolume;

    public SpotifydLifecycleService(double initialVolume) {
        this.initialVolume = initialVolume;
    }

    public void restart() {
        if (isRunning()) {
            cmdService.runCmdCommand("killall", "spotifyd");
        }

        startAndConnect();
        register();
        openStartupUri();
    }

    private void openStartupUri() {
        // silently start playing the playlist, then seek to position 0 to make up for delay between playerctl open and playerctl pause
        setVolume(0);
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "open", Config.spotifyOpenOnStartupUri());
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "pause");
        setVolume(initialVolume);
        try {
            cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "position", "0");
        } catch (CmdServiceException e) {
            // this error may appear if nothing started playing (sometimes it does, sometimes not)
            if (!e.cmdErrorMessage().equals("Could not execute command: GDBus.Error:org.freedesktop.DBus.Error.Failed: can set position while nothing is playing")) {
                throw e;
            }
        }
    }

    private void register() {
        // register spotifyd in playerctl players
        String spotifydPid = cmdService.runCmdCommand(
                new StringCmdOutputTransform(),
                "pidof", "spotifyd"
        );
        cmdService.runCmdCommand(
                "dbus-send",
                "--print-reply",
                "--dest=rs.spotifyd.instance" + spotifydPid,
                "/rs/spotifyd/Controls",
                "rs.spotifyd.Controls.TransferPlayback"
        );
    }

    private void startAndConnect() {
        String tempSpotifydLogPath = ResourceUtils.cachePath("spotifyd.log");

        cmdService.runCmdCommand("touch", tempSpotifydLogPath);

        try {
            startSpotifydDetached(tempSpotifydLogPath);
            waitUntilSpotifydConnects(tempSpotifydLogPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            cmdService.runCmdCommand("rm", tempSpotifydLogPath);
        }
    }

    private void startSpotifydDetached(String spotifydLogPath) throws IOException {
        // start spotifyd with nohup to detach it from the JVM process
        String[] startSpotifydCommands = {
                "/bin/sh",
                "-c",
                "nohup spotifyd --no-daemon >> " + spotifydLogPath + " 2>&1 &"
        };
        var startSpotifydProcessBuilder = new ProcessBuilder(startSpotifydCommands);
        startSpotifydProcessBuilder.start();
    }

    private void waitUntilSpotifydConnects(String spotifydLogPath) throws IOException {
        // tails the log file until the target log about successful connection is printed
        String targetLog = "active device is <> with session";

        String[] commands = { "tail", "-f", spotifydLogPath };
        var processBuilder = new ProcessBuilder(commands);

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(targetLog)) {
                    return;
                }
            }
        }
    }

    private boolean isRunning() {
        try {
            cmdService.runCmdCommand("pidof", "spotifyd");
        } catch (CmdServiceException _) {
            return false;
        }

        return true;
    }

    private void setVolume(double volume) {
        cmdService.runCmdCommand("playerctl", "-p", "spotifyd", "volume", String.valueOf(volume));
    }
}
