package org.maks.musicplayer.fifo;

import org.maks.musicplayer.enumeration.FifoCommand;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

public class FifoService {
    public void read(FifoCommandQueue fifoCommandQueue) {
        String fifoFileName = "commands.fifo";
        String fifoPathStr = Paths.get(fifoFileName).toString();

        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fifoPathStr));
                 // fifo file is opened for writing as long as there is at least one writer
                 // this output stream plays a role of a mock writer to hold fifo open
                 OutputStream holdFifoOpenStream = new FileOutputStream(fifoPathStr)) {

                String commandStr;
                while ((commandStr = bufferedReader.readLine()) != null) {
                    FifoCommand command = FifoCommand.fromString(commandStr);
                    fifoCommandQueue.push(command);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(fifoFileName + " not found. Run 'mkfifo " + fifoFileName + "' in the root project dir");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
