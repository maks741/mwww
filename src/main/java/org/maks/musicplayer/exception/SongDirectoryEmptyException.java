package org.maks.musicplayer.exception;

public class SongDirectoryEmptyException extends RuntimeException {

    public SongDirectoryEmptyException(String message) {
        super(message);
    }
}
