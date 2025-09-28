package org.maks.musicplayer.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

public class FifoService {
    public void read() {
        String fifoFileName = "commands.fifo";
        String fifoPathStr = Paths.get(fifoFileName).toString();

        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fifoPathStr));
                 OutputStream holdFifoOpenStream = new FileOutputStream(fifoPathStr)) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println("LINE: " + line);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(fifoFileName + " not found. Run 'mkfifo " + fifoFileName + "' in the root project dir");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
