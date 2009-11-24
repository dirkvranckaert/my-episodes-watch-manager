package be.dvwd.episodeWatcher.exception;

import java.io.IOException;

public class LoginFailedException extends Throwable {
    public LoginFailedException(String message, Throwable e) {
        super(message, e);
    }

    public LoginFailedException(String message) {
        super(message);
    }
}
