package org.maks.mwww_daemon.exception;

public class CmdServiceException extends RuntimeException {
    public CmdServiceException(String message, String errorMessage) {
        super(message);
    }
}
