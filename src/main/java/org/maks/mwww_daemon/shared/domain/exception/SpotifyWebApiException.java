package org.maks.mwww_daemon.shared.domain.exception;

public class SpotifyWebApiException extends RuntimeException {

    private final String responseBody;

    public SpotifyWebApiException(String message, String responseBody) {
        super(message);
        this.responseBody = responseBody;
    }

    public String responseBody() {
        return responseBody;
    }
}
