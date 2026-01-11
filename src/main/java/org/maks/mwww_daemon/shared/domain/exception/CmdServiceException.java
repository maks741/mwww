package org.maks.mwww_daemon.shared.domain.exception;

public class CmdServiceException extends RuntimeException {

    private final String cmdErrorMessage;

    public CmdServiceException(String message, String cmdErrorMessage) {
        super(message);
        this.cmdErrorMessage = cmdErrorMessage;
    }

    public String cmdErrorMessage() {
        return cmdErrorMessage;
    }
}
