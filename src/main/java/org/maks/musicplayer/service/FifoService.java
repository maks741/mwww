package org.maks.musicplayer.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class FifoService {
    public void read() {
        String fifoPathStr = Paths.get("commands.fifo").toString();

        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fifoPathStr))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println("LINE: " + line);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException("commands.fifo not found. Run 'mkfifo commands.fifo' in the root project dir");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
