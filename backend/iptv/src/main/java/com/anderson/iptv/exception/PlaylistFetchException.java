package com.anderson.iptv.exception;

public class PlaylistFetchException extends RuntimeException {
    public PlaylistFetchException(String message) {
        super(message);
    }

    public PlaylistFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
