package org.maks.mwww_daemon.exception;

public class SongDirectoryEmptyException extends RuntimeException {
    public SongDirectoryEmptyException(String message) {
        super(message);
    }
}
