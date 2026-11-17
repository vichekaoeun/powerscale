package io.powerscale.voting.client;

public class AniListUnavailableException extends RuntimeException {

    public AniListUnavailableException(String message) {
        super(message);
    }

    public AniListUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
